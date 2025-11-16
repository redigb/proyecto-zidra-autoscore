import os
from dotenv import load_dotenv
from pathlib import Path
from pocketbase import PocketBase
import pandas as pd

# Cargar .env solo si no existen variables ya definidas (producción)
ENV_PATH = Path(__file__).resolve().parents[2] / ".env"
if ENV_PATH.exists():
    load_dotenv(ENV_PATH)

ZIDRA_URL = os.getenv("URL_API_ZIDRA")
ZIDRA_USER = os.getenv("USUARIO_ZIDRA")
ZIDRA_PASS = os.getenv("PASSWORD_ZIDRA")

# CONEXIÓN A POCKETBASE (con login y reintento automático)
def connect_pocketbase():
    """
    Inicializa conexión con PocketBase usando login (usuario + contraseña)
    Retorna el cliente listo para hacer consultas.
    """
    if not ZIDRA_URL or not ZIDRA_USER or not ZIDRA_PASS:
        raise Exception("Variables .env faltantes: ZIDRA_URL / ZIDRA_USER / ZIDRA_PASS")

    pb = PocketBase(ZIDRA_URL)

    try:
        auth_data = pb.collection('credi_users').auth_with_password(ZIDRA_USER, ZIDRA_PASS)
        print("[OK] Conectado a PocketBase como:", ZIDRA_USER)
    except Exception as e:
        print("[ERROR] Error al autenticar con PocketBase:", str(e))
        raise e
    return pb


# FUNCIÓN GENÉRICA PARA TRAER CUALQUIER COLECCIÓN COMPLETA
def fetch_all_records(collection_name: str):
    """
    Descarga todos los registros de una colección, manejando paginación automáticamente.
    Convierte los PocketBase Record en dict nativos antes de pasarlos a Pandas.
    """
    pb = connect_pocketbase()
    records = []
    page = 1
    per_page = 200  # PocketBase permite 200 por página

    while True:
        res = pb.collection(collection_name).get_list(page=page, per_page=per_page)

        clean_batch = []
        for item in res.items:
            try:
                clean_batch.append(item.export())  # método oficial de PocketBase
            except:
                clean_batch.append(item.__dict__.get("_data", {}))  # fallback

        records.extend(clean_batch)

        if page >= res.total_pages:
            break
        page += 1

    flat_records = []
    for r in records:
        if isinstance(r, list):
            flat_records.extend(r)
        else:
            flat_records.append(r)

    print(f"[OK] Colección '{collection_name}': {len(flat_records)} registros cargados")
    return flat_records


# COLECCIONES
def fetch_contratos():
    data = fetch_all_records("artefacto_prestante_producto")
    return pd.DataFrame(data)

def fetch_pagos():
    data = fetch_all_records("cronograma_de_pagos")
    return pd.DataFrame(data)

def fetch_recibos():
    data = fetch_all_records("recibo_credito")
    return pd.DataFrame(data)


def record_to_dict(record):
    """
    Convierte un Record de PocketBase a dict, compatible con TODAS las versiones.
    """
    if hasattr(record, "export"):
        try:
            return record.export()
        except:
            pass

    if hasattr(record, "dict"):
        try:
            return record.dict()
        except:
            pass

    if "_data" in record.__dict__:
        return record.__dict__["_data"].copy()

    if "data" in record.__dict__:
        return record.__dict__["data"].copy()

    return {k: v for k, v in record.__dict__.items() if not k.startswith("_")}


# FUNCIÓN PRINCIPAL PARA CONSTRUIR DATASET BASE
def get_raw_datasets():
    """
    Trae las 3 tablas coherentes y vinculadas.
    Limita SOLO contratos (2000 recientes).
    Filtra pagos y recibos SOLO para esos contratos.
    """
    pb = connect_pocketbase()

    def fetch_collection(collection):
        page = 1
        per_page = 200
        all_items = []
        while True:
            res = pb.collection(collection).get_list(page=page, per_page=per_page)
            for item in res.items:
                all_items.append(record_to_dict(item))
            if page >= res.total_pages:
                break
            page += 1
        print(f"[OK] {collection}: {len(all_items)} registros cargados")
        return pd.DataFrame(all_items)

    # (1) CONTRATOS → limitar a 2000
    contratos_df = fetch_collection("artefacto_prestante_producto")

    if "created" in contratos_df.columns:
        contratos_df["created"] = pd.to_datetime(contratos_df["created"], errors="coerce")
        contratos_df = contratos_df.sort_values("created", ascending=False)

    MAX_CONTRATOS = 2000
    MIN_CONTRATOS = 1000

    if len(contratos_df) > MAX_CONTRATOS:
        contratos_df = contratos_df.head(MAX_CONTRATOS)
        print(f"[INFO] Contratos reducidos a {MAX_CONTRATOS} (downsampling)")

    if len(contratos_df) < MIN_CONTRATOS:
        raise Exception(f"[ERROR] Solo {len(contratos_df)} contratos. Se requieren al menos {MIN_CONTRATOS}.")

    print(f"[OK] Contratos finales utilizados: {len(contratos_df)}")

    contrato_ids = contratos_df["id"].tolist()

    # (2) PAGOS
    pagos_df = fetch_collection("cronograma_de_pagos")
    if "id_producto" not in pagos_df.columns:
        raise Exception("[ERROR] cronograma_de_pagos no contiene columna id_producto.")
    pagos_df = pagos_df[pagos_df["id_producto"].isin(contrato_ids)]
    print(f"[OK] Pagos filtrados: {len(pagos_df)}")

    # (3) RECIBOS
    recibos_df = fetch_collection("recibo_credito")
    if "id_pago_contrato" not in recibos_df.columns:
        raise Exception("[ERROR] recibo_credito no contiene columna id_pago_contrato.")
    recibos_df = recibos_df[recibos_df["id_pago_contrato"].isin(contrato_ids)]
    print(f"[OK] Recibos filtrados: {len(recibos_df)}")

    return {
        "contratos": contratos_df,
        "pagos": pagos_df,
        "recibos": recibos_df,
    }


if __name__ == "__main__":
    data = get_raw_datasets()
    print("\n[OK] Datos cargados correctamente:")
    for k, v in data.items():
        print(f" - {k}: {len(v)} filas")
