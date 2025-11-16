# ============================================================
# Entrenamiento de modelo de riesgo - LightGBM + Versioning
# - Modo FULL (batch mensual)  -> --mode full
# - Modo INCREMENTAL (update)  -> --mode incremental
# - Target por negocio: 1 si dpd_max_6m > 0 (por defecto)
# - Rolling window + ponderación temporal
# - Riesgo de cohorte para clientes nuevos
# - Métricas + Gráficas + Versionado automático + Reporte PDF
# ============================================================

import os
import json
import argparse
import warnings
from datetime import datetime, timedelta

import numpy as np
import pandas as pd

from sklearn.metrics import (
    roc_auc_score, roc_curve, confusion_matrix,
    precision_score, recall_score, f1_score
)
from sklearn.model_selection import train_test_split
from sklearn.utils import Bunch
import joblib

import matplotlib
matplotlib.use("Agg")  # para generar imágenes sin GUI
import matplotlib.pyplot as plt

# Versioning integrado 
from src.ml import versioning

# LightGBM (con fallback opcional a RandomForest si no está instalado)
try:
    from lightgbm import LGBMClassifier
    _HAS_LGB = True
except Exception:
    from sklearn.ensemble import RandomForestClassifier
    _HAS_LGB = False
    warnings.warn("LightGBM no disponible. Usando RandomForest como fallback.")

# RUTAS
DATA_PARQUET_PATH = "data/dataset_ml_final.parquet"
TRANSFORMER_PATH = "data/transformer.pkl"
MODELS_DIR = "models"
REPORTS_DIR = "reports"
FILES_DIR = "files"

os.makedirs(MODELS_DIR, exist_ok=True)
os.makedirs(REPORTS_DIR, exist_ok=True)
os.makedirs(FILES_DIR, exist_ok=True)


# -------------------- VERSIONADO --------------------
def next_model_version(prefix="model_v", ext=".pkl"):
    existing = [f for f in os.listdir(MODELS_DIR) if f.startswith(prefix) and f.endswith(ext)]
    nums = []
    for f in existing:
        try:
            n = int(f[len(prefix):-len(ext)])
            nums.append(n)
        except Exception:
            pass
    n_next = (max(nums) + 1) if nums else 1
    return n_next


def ks_statistic(y_true, y_score):
    data = pd.DataFrame({"y": y_true, "p": y_score}).sort_values("p")
    pos = (data["y"] == 1).sum()
    neg = (data["y"] == 0).sum()
    if pos == 0 or neg == 0:
        return 0.0
    data["pos_cum"] = (data["y"] == 1).cumsum() / pos
    data["neg_cum"] = (data["y"] == 0).cumsum() / neg
    return float((data["pos_cum"] - data["neg_cum"]).abs().max())


def plot_roc(y_true, y_score, path_png):
    if len(np.unique(y_true)) < 2:
        return
    fpr, tpr, _ = roc_curve(y_true, y_score)
    auc_val = roc_auc_score(y_true, y_score)
    plt.figure(figsize=(6, 5))
    plt.plot(fpr, tpr, label=f"AUC={auc_val:.3f}")
    plt.plot([0, 1], [0, 1], linestyle="--")
    plt.xlabel("FPR")
    plt.ylabel("TPR")
    plt.title("ROC Curve")
    plt.legend(loc="lower right")
    plt.tight_layout()
    plt.savefig(path_png, dpi=150)
    plt.close()


def plot_confusion(y_true, y_pred, path_png):
    cm = confusion_matrix(y_true, y_pred, labels=[0, 1])
    plt.figure(figsize=(5, 4))
    plt.imshow(cm, interpolation='nearest')
    plt.title("Confusion Matrix")
    plt.colorbar()
    tick_marks = np.arange(2)
    plt.xticks(tick_marks, ["0", "1"])
    plt.yticks(tick_marks, ["0", "1"])
    thresh = cm.max() / 2.0 if cm.size else 0
    for i in range(cm.shape[0]):
        for j in range(cm.shape[1]):
            plt.text(
                j, i, format(cm[i, j], "d"),
                horizontalalignment="center",
                color="white" if cm[i, j] > thresh else "black"
            )
    plt.ylabel("Real")
    plt.xlabel("Predicho")
    plt.tight_layout()
    plt.savefig(path_png, dpi=150)
    plt.close()


# -------------------- CARGA Y PREP --------------------
def load_base_dataset(path=DATA_PARQUET_PATH):
    if not os.path.exists(path):
        raise FileNotFoundError(
            f"No se encontró dataset base en {path}. Ejecuta primero data_prep.py"
        )
    df = pd.read_parquet(path)
    return df


def apply_rolling_window(df: pd.DataFrame, months: int):
    if "registro_contrato" in df.columns:
        fecha_col = pd.to_datetime(df["registro_contrato"], errors="coerce")
        cutoff = pd.Timestamp(datetime.now()).tz_localize(None) - pd.Timedelta(days=30*months)
        if hasattr(fecha_col.dtype, "tz"):
            mask = fecha_col.dt.tz_localize(None) >= cutoff
        else:
            mask = fecha_col >= cutoff
        return df.loc[mask].copy()
    return df.copy()


def compute_time_weights(df: pd.DataFrame, alpha: float = 0.05):
    if "registro_contrato" not in df.columns:
        return np.ones(len(df), dtype=float)

    fecha = pd.to_datetime(df["registro_contrato"], errors="coerce")
    fecha = fecha.dt.tz_localize(None)
    months_ago = (pd.Timestamp(datetime.now()).tz_localize(None) - fecha).dt.days / 30.0
    months_ago = months_ago.fillna(months_ago.max() if len(months_ago) > 0 else 0)
    weights = np.exp(-alpha * months_ago)
    return weights.values


def add_target_and_cohorts(df: pd.DataFrame, dpd_threshold: int = 0, include_pseudolabels: bool = False) -> Bunch:
    df = df.copy()

    if "tipo_cliente" not in df.columns:
        df["tipo_cliente"] = np.where(df["dpd_max_6m"].notna(), "existente", "nuevo")

    df["target"] = np.where(df["dpd_max_6m"].fillna(0) > dpd_threshold, 1, 0)

    base_hist = df[df["tipo_cliente"] == "existente"].copy()
    base_rate = base_hist["target"].mean() if len(base_hist) > 0 else 0.1

    def tasa_por(col):
        if col not in base_hist.columns:
            return {}
        return base_hist.groupby(col)["target"].mean().to_dict()

    c_prod = tasa_por("tipo_contrato")
    c_agente = tasa_por("agente_acreditario")
    c_mes = tasa_por("mes_colocacion")

    def riesgo_fila(row):
        vals = []
        if row.get("tipo_contrato") in c_prod:
            vals.append(c_prod[row["tipo_contrato"]])
        if row.get("agente_acreditario") in c_agente:
            vals.append(c_agente[row["agente_acreditario"]])
        if row.get("mes_colocacion") in c_mes:
            vals.append(c_mes[row["mes_colocacion"]])
        return float(np.mean(vals)) if vals else base_rate

    mask_nuevos = df["tipo_cliente"] == "nuevo"
    if mask_nuevos.any():
        df.loc[mask_nuevos, "riesgo_cohorte"] = df[mask_nuevos].apply(riesgo_fila, axis=1)
    else:
        df["riesgo_cohorte"] = np.nan

    if include_pseudolabels and mask_nuevos.any():
        thr = float(base_rate)
        df.loc[mask_nuevos, "target"] = (
            df.loc[mask_nuevos, "riesgo_cohorte"].fillna(base_rate) >= thr
        ).astype(int)

    return Bunch(df=df, base_rate=float(base_rate))


# -------------------- FEATURES X, y --------------------
LOW_CARD_CATS = [
    "estado_de_prestamo", "tiempo_de_pago", "tipo_contrato",
    "frecuencia_pago", "agente_acreditario", "vehiculo_combustible",
    "vehiculo_clase"
]

ID_LIKE = {
    "id", "codigo_contrato", "id_prestante", "id_registrador", "id_aval",
    "id_conyugue", "numero_dni", "nombre_apellido_prestante",
    "imagenes_producto", "datos_bien", "created", "updated",
    "registro_contrato", "collection_id", "collection_name",
    "ubicacion_producto"
}

LEAKY = {
    "dpd_max_6m", "dpd_prom_6m", "pagos_esperados_6m",
    "pagos_realizados_6m", "ratio_pago_cuotas_6m",
    "dias_desde_ultimo_pago_a_tiempo", "target", "riesgo_cohorte"
}


def build_X_y(df: pd.DataFrame):
    y = df["target"].astype(int).values

    if os.path.exists(TRANSFORMER_PATH):
        transformer = joblib.load(TRANSFORMER_PATH)
        X = transformer.transform(df)
        feature_cols = (
            list(transformer.get_feature_names_out())
            if hasattr(transformer, "get_feature_names_out")
            else []
        )
        return X, y, feature_cols

    # Fallback artesanal
    cols = [c for c in df.columns if c not in ID_LIKE and c not in LEAKY]
    num_cols = [c for c in cols if df[c].dtype.kind in "if"]
    cat_cols = [
        c for c in cols
        if (df[c].dtype == "object" or str(df[c].dtype).startswith("category"))
        and c in LOW_CARD_CATS
    ]

    base = df[num_cols + cat_cols].copy()

    for c in num_cols:
        base[c] = pd.to_numeric(base[c], errors="coerce").fillna(0)
    for c in cat_cols:
        base[c] = base[c].fillna("desconocido").astype(str)

    X_df = pd.get_dummies(base, columns=cat_cols, drop_first=True)
    X = X_df.values
    feature_cols = list(X_df.columns)
    return X, y, feature_cols


# -------------------- ENTRENAMIENTO FULL --------------------
def train_full(df: pd.DataFrame, args):
    if args.rolling_months > 0:
        df = apply_rolling_window(df, args.rolling_months)
        print(f"[INFO] Rolling window aplicada: ultimos {args.rolling_months} meses -> {len(df)} filas")

    b = add_target_and_cohorts(
        df,
        dpd_threshold=args.dpd_threshold,
        include_pseudolabels=args.include_pseudolabels
    )
    df = b.df

    df = df[df["target"].isin([0, 1])].copy()
    time_w = compute_time_weights(df, alpha=args.time_decay_alpha)

    X, y, feat_cols = build_X_y(df)

    strat = y if len(np.unique(y)) > 1 else None

    X_tr, X_te, y_tr, y_te, w_tr, w_te = train_test_split(
        X, y, time_w,
        test_size=0.2,
        random_state=42,
        stratify=strat
    )

    # ---------- MODEL -------------
    if _HAS_LGB:
        from lightgbm import LGBMClassifier, early_stopping

        model = LGBMClassifier(
            learning_rate=args.learning_rate,
            n_estimators=args.num_boost_round,
            num_leaves=31,
            subsample=0.8,
            colsample_bytree=0.9,
            random_state=42
        )

        model.fit(
            X_tr, y_tr,
            sample_weight=w_tr,
            eval_set=[(X_te, y_te)],
            eval_metric="auc",
            callbacks=[early_stopping(stopping_rounds=50, verbose=False)]
        )
        y_proba = model.predict_proba(X_te)[:, 1]

    else:
        from sklearn.ensemble import RandomForestClassifier

        rf = RandomForestClassifier(
            n_estimators=300,
            max_depth=None,
            class_weight="balanced",
            random_state=42,
            n_jobs=-1
        )
        rf.fit(X_tr, y_tr, sample_weight=w_tr)
        model = rf
        y_proba = rf.predict_proba(X_te)[:, 1]

    # ---------- MÉTRICAS ----------
    auc = roc_auc_score(y_te, y_proba) if len(np.unique(y_te)) > 1 else None
    ks = ks_statistic(y_te, y_proba)
    y_pred = (y_proba >= args.decision_threshold).astype(int)

    f1 = f1_score(y_te, y_pred) if len(np.unique(y_te)) > 1 else None
    prec = precision_score(y_te, y_pred, zero_division=0)
    rec = recall_score(y_te, y_pred, zero_division=0)

    # ---------- GRAFICAS ----------
    vers = next_model_version()
    roc_path = os.path.join(REPORTS_DIR, f"roc_v{vers}.png")
    cm_path = os.path.join(REPORTS_DIR, f"confusion_v{vers}.png")

    plot_roc(y_te, y_proba, roc_path)
    plot_confusion(y_te, y_pred, cm_path)

    # ---------- MODELO ----------
    model_path = os.path.join(MODELS_DIR, f"model_v{vers}.pkl")
    joblib.dump(model, model_path)

    versioning.register_model(vers, model_path, status="INACTIVO")
    versioning.activate_model(vers)
    versioning.cleanup_old_models(keep_last=2)

    # ---------- MÉTRICAS ----------
    metrics = {
        "mode": "full",
        "version": vers,
        "rows_train": int(len(X_tr)),
        "rows_valid": int(len(X_te)),
        "auc": float(auc) if auc is not None else None,
        "ks": float(ks),
        "f1": float(f1) if f1 is not None else None,
        "precision": float(prec),
        "recall": float(rec),
        "decision_threshold": float(args.decision_threshold),
        "base_rate": float(df["target"].mean()),
        "created_at": datetime.now().isoformat()
    }

    metrics_path = os.path.join(REPORTS_DIR, f"metrics_v{vers}.json")
    with open(metrics_path, "w", encoding="utf-8") as f:
        json.dump(metrics, f, indent=2, ensure_ascii=False)

    # Guardar metadata de features
    feat_meta_path = os.path.join(MODELS_DIR, f"features_v{vers}.json")
    try:
        with open(feat_meta_path, "w", encoding="utf-8") as f:
            json.dump({"feature_names": feat_cols}, f, indent=2, ensure_ascii=False)
    except Exception:
        pass

    print(f"[OK] Modelo guardado y ACTIVADO: {model_path}")
    print(f"[INFO] Metricas: AUC={metrics['auc']}, KS={metrics['ks']}, F1={metrics['f1']}, Precision={metrics['precision']}, Recall={metrics['recall']}")

    # Actualizar histórico y PDF consolidado
    print("[INFO] Actualizando histórico de métricas...")
    os.system("python src/ml/metrics.py --update-history")
    os.system("python src/ml/metrics.py --generate-pdf")
    print("[OK] Reportes históricos actualizados en /files/")

    return model_path, vers


# -------------------- INCREMENTAL --------------------
def train_incremental(df: pd.DataFrame, args):
    b = add_target_and_cohorts(
        df,
        dpd_threshold=args.dpd_threshold,
        include_pseudolabels=args.include_pseudolabels
    )
    df = b.df
    df = df[df["target"].isin([0, 1])].copy()

    time_w = compute_time_weights(df, alpha=args.time_decay_alpha)
    X, y, feat_cols = build_X_y(df)

    if not _HAS_LGB:
        warnings.warn("LightGBM no disponible. Usando FULL train.")
        return train_full(df, args)

    last_version = next_model_version() - 1
    last_model_path = os.path.join(MODELS_DIR, f"model_v{last_version}.pkl")

    if not os.path.exists(last_model_path):
        print("[INFO] No existe modelo previo. Ejecutando FULL train.")
        return train_full(df, args)

    init_model = joblib.load(last_model_path)

    booster = None
    if hasattr(init_model, "booster_"):
        booster = init_model.booster_
    elif isinstance(init_model, LGBMClassifier.Booster):
        booster = init_model

    if booster is None:
        print("[WARN] No se pudo reusar booster. Ejecutando FULL train.")
        return train_full(df, args)

    train_set = LGBMClassifier.Dataset(X, label=y, weight=time_w)

    params = dict(
        objective="binary",
        metric=["auc"],
        learning_rate=args.learning_rate,
        num_leaves=31,
        feature_fraction=0.9,
        bagging_fraction=0.8,
        bagging_freq=1,
        min_data_in_leaf=20,
        verbose=-1
    )

    model = LGBMClassifier.train(
        params,
        train_set,
        num_boost_round=max(50, args.num_boost_round // 4),
        init_model=booster,
        keep_training_booster=True,
        valid_sets=[train_set],
        verbose_eval=False
    )

    vers = next_model_version()
    model_path = os.path.join(MODELS_DIR, f"model_v{vers}.pkl")
    joblib.dump(model, model_path)

    versioning.register_model(vers, model_path, status="INACTIVO")
    versioning.activate_model(vers)
    versioning.cleanup_old_models(keep_last=2)

    print(f"[OK] Entrenamiento incremental completo -> model_v{vers}.pkl ACTIVADO")
    os.system("python src/ml/metrics.py --update-history")
    os.system("python src/ml/metrics.py --generate-pdf")
    print("[OK] Reportes históricos actualizados en /files/")

    return model_path, vers


# -------------------- MAIN CLI --------------------
def main():
    parser = argparse.ArgumentParser(description="Entrenamiento de modelo de riesgo (LightGBM).")
    parser.add_argument("--mode", choices=["full", "incremental"], default="full")
    parser.add_argument("--rolling_months", type=int, default=12)
    parser.add_argument("--dpd_threshold", type=int, default=0)
    parser.add_argument("--time_decay_alpha", type=float, default=0.05)
    parser.add_argument("--learning_rate", type=float, default=0.05)
    parser.add_argument("--num_boost_round", type=int, default=500)
    parser.add_argument("--decision_threshold", type=float, default=0.5)
    parser.add_argument("--include_pseudolabels", action="store_true")

    args = parser.parse_args()

    print(
        f"[INFO] Modo={args.mode} | rolling={args.rolling_months} meses | "
        f"dpd>{args.dpd_threshold} | alpha={args.time_decay_alpha}"
    )

    df = load_base_dataset()

    if args.mode == "full":
        train_full(df, args)
    else:
        train_incremental(df, args)


if __name__ == "__main__":
    main()
