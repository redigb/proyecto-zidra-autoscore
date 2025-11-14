import glob
import json
import os
import pickle
import pandas as pd
import joblib
from src.core.config import settings


class ModelService:
    def __init__(self):
        self.model = self._load_latest_model()
        self.transformer = self._load_transformer()

    #  CARGA DE MODELO Y TRANSFORMER
    def _load_latest_model(self):
        pattern = str(settings.MODELS_DIR / "model_v*.pkl")
        files = glob.glob(pattern)

        if not files:
            raise RuntimeError(f"No se encontraron modelos con patrón {pattern}")

        latest = sorted(files)[-1]
        version = int(os.path.basename(latest).split("model_v")[1].split(".pkl")[0])
        self.version = version

        print(f"📌 Cargando modelo activo: {latest}")
        
        return joblib.load(latest)

    def _load_transformer(self):
        transformer_path = settings.TRANSFORMER_PATH

        if not transformer_path.exists():
            print("⚠ No hay transformer, usando procesamiento fallback")
            return None

        print(f"📌 Cargando transformer: {transformer_path}")

        import joblib
        return joblib.load(transformer_path)

    # SCORING 
    def predict(self, payload: dict) -> dict:
        df = pd.DataFrame([payload])
        X = self.transformer.transform(df)
        proba = float(self.model.predict_proba(X)[0][1])

        riesgo = (
            "BAJO" if proba < 0.33 else
            "MEDIO" if proba < 0.66 else
            "ALTO"
        )

        return {
            "score": proba,
            "riesgo": riesgo,
            "version": self.version
        }

    # MÉTRICAS E HISTÓRICO 
    def load_metrics_history(self):
        csv_path = settings.METRICS_HISTORY_CSV
        if not csv_path.exists():
            raise RuntimeError(f"No se encontró historial_metricas.csv en {csv_path}")

        df = pd.read_csv(csv_path)
        return df

    def get_active_model_info(self):
        df = self.load_metrics_history()
        # tomamos la última versión registrada
        last = df.sort_values("version").iloc[-1]
        return last.to_dict()

    # FEATURES POR VERSIÓN 
    def get_features_for_version(self, version: int):
        features_file = settings.MODELS_DIR / settings.MODEL_FEATURES_PATTERN.format(version=version)
        if not features_file.exists():
            raise FileNotFoundError(f"No existe archivo de features para v{version}: {features_file}")

        with open(features_file, "r", encoding="utf-8") as f:
            return json.load(f)

    # REGISTRO DE MODELOS
    def get_model_registry(self):
        path = settings.MODEL_REGISTRY_JSON
        if not path.exists():
            raise FileNotFoundError(f"No se encontró model_registry.json en {path}")

        with open(path, "r", encoding="utf-8") as f:
            return json.load(f)


model_service = ModelService()
