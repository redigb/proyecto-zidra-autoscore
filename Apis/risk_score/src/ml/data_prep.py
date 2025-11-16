import os
import json
import pandas as pd
import numpy as np
from datetime import datetime, timedelta

pd.options.mode.chained_assignment = None

from src.ml.data_source_api import get_raw_datasets

# Colores CLI (estos SI funcionan bien)
GREEN = "\033[92m"
YELLOW = "\033[93m"
RED = "\033[91m"
BLUE = "\033[94m"
BOLD = "\033[1m"
RESET = "\033[0m"


# Utilidades internas
def _tz_naive(ts):
    if pd.isna(ts):
        return pd.NaT
    ts = pd.to_datetime(ts, errors="coerce")
    if ts is pd.NaT:
        return ts
    try:
        return ts.tz_localize(None)
    except Exception:
        return ts

def _json_like_to_str(x):
    if isinstance(x, (list, dict)):
        try:
            return json.dumps(x, ensure_ascii=False)
        except Exception:
            return str(x)
    return x

def _first_item_dict(x):
    if isinstance(x, list) and len(x) > 0 and isinstance(x[0], dict):
        return x[0]
    if isinstance(x, dict):
        return x
    return None

def extract_feature(x, key):
    d = _first_item_dict(x)
    if d and isinstance(d, dict):
        return d.get(key, None)
    return None


# 1) Cargar datos crudos
def load_raw_data():
    raw = get_raw_datasets()
    print(f"\n{GREEN}[OK] Datos crudos cargados desde PocketBase.{RESET}")
    print(f"\n{BLUE}[INFO] RAW TYPE CHECK:{RESET}")
    for name, df in raw.items():
        print(f"   - {name}: type={type(df)}, shape={df.shape}")
        print(f"     - Columnas: {list(df.columns)[:10]}...")
    return raw


# 2) Detectar clientes nuevos/existentes
def detectar_clientes_nuevos(contratos_df, pagos_df):
    contrato_map = contratos_df[["id", "numero_dni"]].rename(columns={"id": "id_producto"})
    pagos_df = pagos_df.merge(contrato_map, on="id_producto", how="left")

    clientes_hist = pagos_df["numero_dni"].dropna().unique()

    contratos_df["tipo_cliente"] = contratos_df["numero_dni"].apply(
        lambda dni: "existente" if dni in clientes_hist else "nuevo"
    )

    n_nuevos = (contratos_df["tipo_cliente"] == "nuevo").sum()
    n_exist = (contratos_df["tipo_cliente"] == "existente").sum()

    print("\n[INFO] Detección de clientes:")
    print(f"   - Clientes nuevos: {n_nuevos}")
    print(f"   - Clientes existentes: {n_exist}")

    return contratos_df, pagos_df


# 3) Features de contratos
def features_contratos(contratos_df):
    df = contratos_df.copy()

    df["monto_total"] = pd.to_numeric(df.get("coste_saldo_total", 0), errors="coerce")
    df["cuota_inicial"] = pd.to_numeric(df.get("cuota_inicial", 0), errors="coerce")
    df["plazo_meses"] = pd.to_numeric(df.get("numero_cuotas", 0), errors="coerce")
    df["frecuencia_pago"] = df.get("tiempo_de_pago", "mensual").fillna("mensual")

    df["mes_colocacion"] = pd.to_datetime(df.get("registro_contrato"), errors="coerce").dt.month

    df["bien_tipo"] = df["datos_bien"].apply(lambda x: extract_feature(x, "artefacto"))
    df["bien_marca"] = df["datos_bien"].apply(lambda x: extract_feature(x, "marca"))
    df["bien_modelo"] = df["datos_bien"].apply(lambda x: extract_feature(x, "modelo"))
    df["bien_precio"] = df["datos_bien"].apply(
        lambda x: pd.to_numeric(extract_feature(x, "precio"), errors="coerce")
    )

    is_veh = (df.get("tipo_contrato", "").astype(str).str.lower() == "vehicular")
    db_norm = df["datos_bien"].apply(_first_item_dict)

    df.loc[is_veh, "vehiculo_marca"] = db_norm[is_veh].apply(lambda d: None if d is None else d.get("marca"))
    df.loc[is_veh, "vehiculo_modelo"] = db_norm[is_veh].apply(lambda d: None if d is None else d.get("modelo"))
    df.loc[is_veh, "vehiculo_ano_modelo"] = pd.to_numeric(
        db_norm[is_veh].apply(lambda d: None if d is None else d.get("anoModelo")), errors="coerce"
    )
    df.loc[is_veh, "vehiculo_cilindrada"] = db_norm[is_veh].apply(lambda d: None if d is None else d.get("cilindrada"))
    df.loc[is_veh, "vehiculo_clase"] = db_norm[is_veh].apply(lambda d: None if d is None else d.get("clase"))
    df.loc[is_veh, "vehiculo_combustible"] = db_norm[is_veh].apply(lambda d: None if d is None else d.get("combustible"))
    df.loc[is_veh, "vehiculo_precio"] = pd.to_numeric(
        db_norm[is_veh].apply(lambda d: None if d is None else d.get("montoVehiculo")), errors="coerce"
    )

    df["datos_bien"] = df["datos_bien"].apply(_json_like_to_str)
    df["aval_prestante"] = df.get("aval_prestante", np.nan).apply(_json_like_to_str)
    df["conyugue_prestante"] = df.get("conyugue_prestante", np.nan).apply(_json_like_to_str)

    df.fillna({"cuota_inicial": 0, "plazo_meses": 0, "monto_total": 0}, inplace=True)

    print(f"[OK] Features de contrato generadas: {df.shape[0]} filas.")
    return df


# 4) Features historial de pagos
def features_historial(pagos_df, recibos_df):
    df = pagos_df.copy()

    df["fecha_pago_programada"] = df.get("fecha_de_pago").apply(_tz_naive)
    df["fecha_pago_real"] = df.get("fecha_cancelada").apply(_tz_naive)

    cutoff = pd.Timestamp(datetime.now()).tz_localize(None) - pd.Timedelta(days=180)
    df_recent = df[df["fecha_pago_programada"] >= cutoff].copy()

    df_recent.loc[:, "dpd"] = (df_recent["fecha_pago_real"] - df_recent["fecha_pago_programada"]).dt.days
    df_recent.loc[:, "dpd"] = df_recent["dpd"].clip(lower=0).fillna(0)

    resumen = df_recent.groupby("numero_dni").agg(
        dpd_max_6m=("dpd", "max"),
        dpd_prom_6m=("dpd", "mean"),
        pagos_esperados_6m=("dpd", "count"),
        pagos_realizados_6m=("fecha_pago_real", "count"),
    ).reset_index()

    resumen["ratio_pago_cuotas_6m"] = (
        resumen["pagos_realizados_6m"] / resumen["pagos_esperados_6m"]
    ).fillna(0)

    pagos_a_tiempo = df_recent[df_recent["dpd"] == 0].groupby("numero_dni")["fecha_pago_real"].max()

    ultimos_pagos = pagos_a_tiempo.reindex(resumen["numero_dni"])
    ultimos_pagos = pd.to_datetime(ultimos_pagos, errors="coerce")

    now = pd.Timestamp(datetime.now()).tz_localize(None)
    resumen["dias_desde_ultimo_pago_a_tiempo"] = (now - ultimos_pagos).dt.days.fillna(999)

    print(f"[OK] Features de comportamiento generadas: {resumen.shape[0]} clientes.")
    return resumen


# 5) Features avanzadas
def add_advanced_features(df_final, pagos_df):
    print(f"\n{BLUE}[INFO] Generando features avanzadas...{RESET}")

    print("   - Calculando n_contratos_previos...")
    contratos_por_dni = df_final.groupby("numero_dni")["id"].count().to_dict()
    df_final["n_contratos_previos"] = df_final["numero_dni"].map(contratos_por_dni).fillna(0)

    print("   - Calculando veces_con_mora_hist...")
    mora_counts = pagos_df[pagos_df["estado"] == "atrasado"].groupby("numero_dni")["estado"].count().to_dict()
    df_final["veces_con_mora_hist"] = df_final["numero_dni"].map(mora_counts).fillna(0)

    print("   - Generando variables limpias de cierre...")
    if "estado_de_prestamo" in df_final.columns:
        df_final["cierre_exitoso"] = df_final["estado_de_prestamo"].isin(["pagado", "cierre de pago"]).astype(int)
        df_final["termino_con_deuda"] = df_final["estado_de_prestamo"].isin(["deuda", "recogido", "anulado"]).astype(int)
        df_final["tuvo_mora_pero_pago"] = (
            (df_final["termino_con_deuda"] == 0) &
            (df_final["veces_con_mora_hist"] > 0) &
            (df_final["cierre_exitoso"] == 1)
        ).astype(int)
    else:
        print("[WARN] No existe 'estado_de_prestamo'. Asignando 0.")
        df_final["cierre_exitoso"] = 0
        df_final["termino_con_deuda"] = 0
        df_final["tuvo_mora_pero_pago"] = 0

    print("   - Calculando riesgo_cohorte...")
    df_final["is_moroso"] = (df_final["dpd_max_6m"] > 0).astype(int)
    cohorte_riesgo = df_final.groupby(["tipo_contrato"])["is_moroso"].mean().to_dict()
    df_final["riesgo_cohorte"] = df_final["tipo_contrato"].map(
        lambda x: cohorte_riesgo.get(x, 0)
    ).fillna(0)

    print(f"{GREEN}[OK] Features avanzadas añadidas.{RESET}")
    return df_final


# 5) Dataset final
def build_dataset_final():
    raw = load_raw_data()

    contratos_df, pagos_df_enriquecido = detectar_clientes_nuevos(raw["contratos"], raw["pagos"])

    contratos_df = features_contratos(contratos_df)
    historial_df = features_historial(pagos_df_enriquecido, raw["recibos"])

    df_final = contratos_df.merge(historial_df, on="numero_dni", how="left")

    df_final.fillna({
        "dpd_max_6m": 0,
        "dpd_prom_6m": 0,
        "ratio_pago_cuotas_6m": 0,
        "dias_desde_ultimo_pago_a_tiempo": 999
    }, inplace=True)

    df_final = add_advanced_features(df_final, pagos_df_enriquecido)

    advanced_cols = ["n_contratos_previos", "veces_con_mora_hist", "riesgo_cohorte"]
    cols = list(df_final.columns)
    df_final = df_final[[c for c in cols if c not in advanced_cols] + advanced_cols]

    print(f"\n{GREEN}[OK] Dataset final generado: {df_final.shape[1]} columnas.{RESET}")
    return df_final


# 6) Ejecución directa
if __name__ == "__main__":
    os.makedirs("data", exist_ok=True)

    df_final = build_dataset_final()
    df_final = df_final[df_final["estado_de_prestamo"] != "anulado"]

    cols_to_drop_for_parquet = ["expand", "collection_id", "collection_name"]
    df_final = df_final.drop(columns=[c for c in cols_to_drop_for_parquet if c in df_final.columns], errors="ignore")
    print("[INFO] Columnas eliminadas para compatibilidad con Parquet:", cols_to_drop_for_parquet)

    parquet_path = "data/dataset_ml_final.parquet"
    csv_path = "data/dataset_ml_final.csv"

    try:
        df_final.to_parquet(parquet_path, index=False)
        print(f"{GREEN}[SAVE] Exportado Parquet: {parquet_path}{RESET}")
    except Exception as e:
        print(f"{YELLOW}[WARN] No se pudo exportar a Parquet ({type(e).__name__}: {e}). Guardando CSV...{RESET}")
        df_final.to_csv(csv_path, index=False, encoding="utf-8")
        print(f"{GREEN}[SAVE] Exportado CSV: {csv_path}{RESET}")

    print(f"{BOLD}{GREEN}[OK] DATASET ML GENERADO Y GUARDADO{RESET}")
