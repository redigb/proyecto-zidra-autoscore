import os
from dotenv import load_dotenv
from pathlib import Path

# Cargar .env solo si no existen variables ya definidas (producción)
ENV_PATH = Path(__file__).resolve().parents[2] / ".env"
if ENV_PATH.exists():
    load_dotenv(ENV_PATH)

BASE_DIR = Path(__file__).resolve().parents[2]

class Settings:

    class Config:
        env_file = ".env"

    # Paths base
    BASE_DIR: Path = BASE_DIR
    MODELS_DIR: Path = BASE_DIR / "models"
    DATA_DIR: Path = BASE_DIR / "data"
    FILES_DIR: Path = BASE_DIR / "files"

    # Archivos
    TRANSFORMER_PATH: Path = DATA_DIR / "transformer.pkl"
    METRICS_HISTORY_CSV: Path = FILES_DIR / "historial_metricas.csv"
    MODEL_REGISTRY_JSON: Path = MODELS_DIR / "model_registry.json"
    MODEL_FEATURES_PATTERN: str = "features_v{version}.json"
    REPORT_PDF_PATH: Path = FILES_DIR / "report_evolucion_modelo.pdf"

settings = Settings()
