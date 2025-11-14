import os
import json
import argparse
from datetime import datetime

MODELS_DIR = "models"
REGISTRY_PATH = os.path.join(MODELS_DIR, "model_registry.json")

os.makedirs(MODELS_DIR, exist_ok=True)

# ============================================================
# Cargar o crear registro de modelos
# ============================================================
def load_registry():
    if not os.path.exists(REGISTRY_PATH):
        return []
    with open(REGISTRY_PATH, "r", encoding="utf-8") as f:
        return json.load(f)

def save_registry(registry):
    with open(REGISTRY_PATH, "w", encoding="utf-8") as f:
        json.dump(registry, f, indent=2, ensure_ascii=False)

# ============================================================
# Registrar un nuevo modelo
# ============================================================
def register_model(version, path, status="INACTIVO"):
    registry = load_registry()
    registry.append({
        "version": version,
        "path": path,
        "status": status,
        "registered_at": datetime.now().isoformat()
    })
    save_registry(registry)
    print(f"✅ Modelo v{version} registrado con estado '{status}'")

# ============================================================
# Activar un modelo (el anterior pasa a RESERVA)
# ============================================================
def activate_model(version):
    registry = load_registry()
    updated = False
    for entry in registry:
        if entry["version"] == version:
            entry["status"] = "ACTIVO"
            updated = True
        elif entry["status"] == "ACTIVO":
            entry["status"] = "RESERVA"
        else:
            entry["status"] = "ARCHIVADO"
    if not updated:
        print(f"⚠ No se encontró el modelo v{version} en el registro")
        return
    save_registry(registry)
    print(f"🚀 Modelo v{version} activado correctamente")

# ============================================================
# Limpiar modelos antiguos (mantener solo 2)
# ============================================================
def cleanup_old_models(keep_last=2):
    registry = load_registry()
    registry_sorted = sorted(registry, key=lambda x: x["version"], reverse=True)

    # Mantener solo los más recientes
    keep = registry_sorted[:keep_last]
    remove = registry_sorted[keep_last:]

    # Dar aviso o eliminar físicamente
    for entry in remove:
        path = entry["path"]
        if os.path.exists(path):
            os.remove(path)
            print(f"🗑 Modelo antiguo eliminado: {path}")
        entry["status"] = "ELIMINADO"

    # Guardar nuevo estado
    save_registry(keep)
    print(f"♻ Limpieza completada. Manteniendo {keep_last} versiones más recientes.")

# ============================================================
# Obtener modelo activo (para FastAPI / scoring)
# ============================================================
def get_active_model_path():
    registry = load_registry()
    for entry in registry:
        if entry["status"] == "ACTIVO":
            return entry["path"]
    print("⚠ No hay modelo activo en el registro")
    return None

# ============================================================
# CLI MANUAL
# ============================================================
def cli():
    parser = argparse.ArgumentParser(description="Gestión de versiones del modelo de riesgo")
    parser.add_argument("--show", action="store_true", help="Mostrar registro de modelos")
    parser.add_argument("--activate", type=int, help="Activar modelo por versión")
    parser.add_argument("--cleanup", action="store_true", help="Eliminar versiones antiguas")

    args = parser.parse_args()

    if args.show:
        registry = load_registry()
        print(json.dumps(registry, indent=2, ensure_ascii=False))

    if args.activate is not None:
        activate_model(args.activate)

    if args.cleanup:
        cleanup_old_models()

if __name__ == "__main__":
    cli()
