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
        print("Conectado a PocketBase como:", ZIDRA_USER)
    except Exception as e:
        print("Error al autenticar con PocketBase:", str(e))
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

    # Asegurarse de que la lista final es completamente plana y compuesta solo por dicts
    flat_records = []
    for r in records:
        if isinstance(r, list):
            flat_records.extend(r)
        else:
            flat_records.append(r)

    print(f"Colección '{collection_name}' → {len(flat_records)} registros cargados")
    return flat_records

# COLECCIONES
def fetch_contratos():
    """
    Carga datos de artefacto_prestante_producto → DataFrame listo para uso.
    """
    data = fetch_all_records("artefacto_prestante_producto")
    return pd.DataFrame(data)

def fetch_pagos():
    """
    Carga datos de cronograma_de_pagos → DataFrame listo para uso.
    """
    data = fetch_all_records("cronograma_de_pagos")
    return pd.DataFrame(data)

def fetch_recibos():
    """
    Carga pagos reales registrados en recibo_credito → DataFrame.
    """
    data = fetch_all_records("recibo_credito")
    return pd.DataFrame(data)

def record_to_dict(record):
    """
    Convierte un Record de PocketBase a dict, compatible con TODAS las versiones.
    """
    # 1. Versiones modernas (tienen export)
    if hasattr(record, "export"):
        try:
            return record.export()
        except:
            pass
    # 2. Versiones con dict() oficial
    if hasattr(record, "dict"):
        try:
            return record.dict()
        except:
            pass
    # 3. Versiones antiguas (guardan en __dict__["data"])
    if "_data" in record.__dict__:
        return record.__dict__["_data"].copy()

    if "data" in record.__dict__:
        return record.__dict__["data"].copy()
    # 4. Fallback universal
    return {k: v for k, v in record.__dict__.items() if not k.startswith("_")}

# FUNCIÓN BASE PARA CONSTRUIR DATASET FINAL DEL MODELO
# (por ahora solo trae las tablas, luego haremos merge con lógica ML)
def get_raw_datasets():
    """
    Trae las 3 tablas coherentes y vinculadas.
    Limita SOLO contratos (2000 recientes).
    Filtra pagos y recibos SOLO para esos contratos.
    """
    pb = connect_pocketbase()
    # Función universal para extraer colección
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
        print(f"✅ {collection} — registros cargados: {len(all_items)}")
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
        print(f"⚠️ Downsampling contratos a {MAX_CONTRATOS}")
    if len(contratos_df) < MIN_CONTRATOS:
        raise Exception(f"Solo {len(contratos_df)} contratos. Se requieren {MIN_CONTRATOS}.")
    print(f"📌 Contratos finales: {len(contratos_df)}")
    contrato_ids = contratos_df["id"].tolist()
    # (2) PAGOS → enlazar por id_producto
    pagos_df = fetch_collection("cronograma_de_pagos")
    if "id_producto" not in pagos_df.columns:
        raise Exception("ERROR: cronograma_de_pagos no contiene id_producto.")
    pagos_df = pagos_df[pagos_df["id_producto"].isin(contrato_ids)]
    print(f"📌 Pagos filtrados por contratos: {len(pagos_df)}")
    
    # (3) RECIBOS → enlazar DIRECTO por id_pago_contrato
    recibos_df = fetch_collection("recibo_credito")

    if "id_pago_contrato" not in recibos_df.columns:
        raise Exception("❌ ERROR: recibo_credito no contiene id_pago_contrato.")
    
    recibos_df = recibos_df[recibos_df["id_pago_contrato"].isin(contrato_ids)]
    print(f"📌 Recibos filtrados por contratos: {len(recibos_df)}")

    return {
        "contratos": contratos_df,
        "pagos": pagos_df,
        "recibos": recibos_df,
    }

if __name__ == "__main__":
    # ✅ Test rápido de conexión
    data = get_raw_datasets()
    print("\n✅ Datos cargados correctamente:")
    for k, v in data.items():
        print(f" - {k}: {len(v)} filas")
