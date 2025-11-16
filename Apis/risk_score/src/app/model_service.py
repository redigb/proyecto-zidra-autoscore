import glob
import json
import os
import pandas as pd
import joblib
from src.core.config import settings


class ModelService:
    def __init__(self):
        # No cargamos nada aquí todavía (porque uvicorn --reload rompe esto)
        self.model = None
        self.transformer = None
        self.registry_list = None
        self.active_version = None



    #  Lazy-load automático (robusto contra reload de Uvicorn)
    def ensure_loaded(self):
        """
        Se asegura de que el modelo, transformer y registry estén cargados.
        Se ejecuta antes de cualquier operación crítica (score, info, etc.)
        """
        if self.model is None or self.transformer is None or self.registry_list is None:
            print("⏳ Inicializando motor ML...")

            self._load_registry()
            self.model = self._load_active_model()
            self.transformer = self._load_transformer()

            print("✅ Motor ML listo para scoring.")



    #                   REGISTRO DE MODELOS
    def _load_registry(self):
        path = settings.MODEL_REGISTRY_JSON

        if not path.exists():
            raise FileNotFoundError("⚠ model_registry.json no existe — ejecute /models/train primero")

        with open(path, "r", encoding="utf-8") as f:
            data = json.load(f)

        self.registry_list = data
        active = next((m for m in data if m.get("status") == "ACTIVO"), None)

        self.active_version = active["version"] if active else None

        return data


  
    #                 CARGA DEL MODELO ACTIVO
    def _load_active_model(self):
        if self.active_version is None:
            raise RuntimeError("❌ No hay modelo activo registrado")

        model_path = settings.MODELS_DIR / f"model_v{self.active_version}.pkl"

        if not model_path.exists():
            raise FileNotFoundError(f"❌ Archivo de modelo no encontrado: {model_path}")

        print(f"📌 Cargando modelo activo: v{self.active_version}")
        return joblib.load(model_path)


    #                      CARGA DEL TRANSFORMER
    def _load_transformer(self):
        transformer_path = settings.TRANSFORMER_PATH

        if not transformer_path.exists():
            raise RuntimeError("❌ transformer.pkl no existe — ejecute /models/train")

        print(f"📌 Cargando transformer: {transformer_path}")
        return joblib.load(transformer_path)



    #                           SCORING
    def predict(self, payload: dict) -> dict:

        # Seguridad → garantiza que todo esté cargado
        self.ensure_loaded()

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
            "version": self.active_version
        }



    #                        MÉTRICAS
    def load_metrics_history(self):
        path = settings.METRICS_HISTORY_CSV

        if not path.exists():
            raise RuntimeError(f"No existe historial métrico en {path}")

        df = pd.read_csv(path)
        df = df.where(df.notna(), None).astype(object)

        return df


    def get_active_model_info(self):

        self.ensure_loaded()

        df = pd.read_csv(settings.METRICS_HISTORY_CSV)

        row = df[df["version"] == self.active_version]
        if row.empty:
            raise RuntimeError(f"No hay métricas para versión {self.active_version}")

        return row.iloc[0].to_dict()


    #                     LISTAR VERSIONES
    def list_versions(self):
        self.ensure_loaded()
        return self.registry_list



    #             FEATURES
    def get_features_for_version(self, version: int):
        features_file = settings.MODELS_DIR / settings.MODEL_FEATURES_PATTERN.format(version=version)

        if not features_file.exists():
            raise FileNotFoundError(f"No existe archivo de features para v{version}: {features_file}")

        with open(features_file, "r", encoding="utf-8") as f:
            return json.load(f)


model_service = ModelService()
