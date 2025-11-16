import pandas as pd
import yaml
import joblib
from sklearn.preprocessing import OneHotEncoder
from sklearn.compose import ColumnTransformer
from sklearn.pipeline import Pipeline
from sklearn.impute import SimpleImputer
import os


# Cargar Feature Spec YAML
def load_feature_spec(path="src/pipeline/feature_spec.yaml"):
    with open(path, "r", encoding="utf-8") as f:
        spec = yaml.safe_load(f)
    return spec["features"]


# Cargar dataset parquet
def load_dataset_parquet(path="data/dataset_ml_final.parquet"):
    if not os.path.exists(path):
        raise FileNotFoundError(f"[ERROR] No se encontró el dataset preprocessado en: {path}")
    print(f"[INFO] Cargando dataset base desde: {path}")
    return pd.read_parquet(path)


# Construir pipeline dinámico
def build_preprocess_pipeline(feature_spec, df):
    numeric_features = []
    categorical_features = []

    for feature, config in feature_spec.items():
        if feature not in df.columns:
            print(f"[WARN] La columna '{feature}' no está en el dataset. Será ignorada.")
            continue

        if feature == "mes_colocacion":
            df["mes_colocacion"] = pd.to_numeric(df["mes_colocacion"], errors="coerce").fillna(0).astype(int)

        if config["tipo"] == "numerica":
            numeric_features.append(feature)

        elif config["tipo"] == "categorica":
            categorical_features.append(feature)

    transformers = []

    # Numéricas → imputar media
    if numeric_features:
        transformers.append(
            (
                "numeric_imputer",
                SimpleImputer(strategy="mean"),
                numeric_features
            )
        )

    # Categóricas → imputar + onehot
    if categorical_features:
        transformers.append(
            (
                "categorical_encoder",
                Pipeline(steps=[
                    ("imputer", SimpleImputer(strategy="constant", fill_value="desconocido")),
                    ("onehot", OneHotEncoder(handle_unknown="ignore", sparse_output=False))
                ]),
                categorical_features
            )
        )

    print("[OK] Transformadores generados dinámicamente según el YAML")
    return ColumnTransformer(transformers=transformers, remainder="drop")


# Preprocesar dataset completo
def preprocess_dataset():
    spec = load_feature_spec()
    df = load_dataset_parquet()

    df = df[[f for f in spec.keys() if f in df.columns]]
    print(f"[INFO] Usando {len(df.columns)} columnas definidas en feature_spec.yaml")

    preprocess_pipeline = build_preprocess_pipeline(spec, df)

    print("[INFO] Ajustando transformaciones...")
    X_transformed = preprocess_pipeline.fit_transform(df)

    os.makedirs("data", exist_ok=True)
    joblib.dump(preprocess_pipeline, "data/transformer.pkl")
    joblib.dump(X_transformed, "data/X_ready.pkl")

    print("[OK] Preprocesamiento finalizado.")
    print("[INFO] Guardado: data/X_ready.pkl y data/transformer.pkl")


# MAIN
if __name__ == "__main__":
    preprocess_dataset()
