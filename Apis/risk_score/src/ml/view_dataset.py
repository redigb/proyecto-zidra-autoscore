import pandas as pd
import numpy as np

GREEN = "\033[92m"
YELLOW = "\033[93m"
RED = "\033[91m"
BLUE = "\033[94m"
RESET = "\033[0m"
BOLD = "\033[1m"

PATH = "data/dataset_ml_final.parquet"

def classify_feature(dtype, unique_count, nulls):
    """Devuelve una etiqueta de recomendación ML."""
    if str(dtype).startswith(("int", "float")):
        if nulls > 0:
            return f"{YELLOW}Numérico → Imputar nulos y escalar{RESET}"
        return f"{GREEN}Numérico → Escalar/Normalizer{RESET}"
    else:
        if unique_count < 30:
            return f"{GREEN}Categórico → OneHot / LabelEncoder{RESET}"
        else:
            return f"{YELLOW}Categórico alta cardinalidad → Target Encoding{RESET}"

def is_unhashable(x):
    """Detecta listas, dicts o arrays que rompen .nunique()"""
    return isinstance(x, (list, dict, np.ndarray))

if __name__ == "__main__":
    df = pd.read_parquet(PATH)

    print(f"\n{GREEN}✅ Dataset cargado desde {PATH}{RESET}")
    print(f"👉 Shape: {df.shape[0]} filas x {df.shape[1]} columnas\n")

    print(f"{BLUE}{BOLD}📊 Análisis de columnas:{RESET}")
    print("-" * 80)

    for idx, col in enumerate(df.columns):
        # ✅ FIX: proteger antes de nunique()
        if df[col].apply(is_unhashable).any():
            df[col] = df[col].apply(lambda x: str(x))

        dtype = df[col].dtype
        unique_vals = df[col].nunique()
        null_count = df[col].isna().sum()
        recomendacion = classify_feature(dtype, unique_vals, null_count)

        print(f"{idx:02d} → {BOLD}{col}{RESET}")
        print(f"     • Tipo: {dtype}")
        print(f"     • Valores únicos: {unique_vals}")
        print(f"     • Nulos: {null_count}")
        print(f"     • 🎯 Recomendación: {recomendacion}")
        print("-" * 80)
