import os
import json
import glob
import pandas as pd
import matplotlib
matplotlib.use("Agg")  # Para generar PDF/PNG sin GUI
import matplotlib.pyplot as plt
from fpdf import FPDF

REPORTS_DIR = "reports"
FILES_DIR = "files"
os.makedirs(FILES_DIR, exist_ok=True)

# ============================================================
# 1. CARGAR TODAS LAS MÉTRICAS GUARDADAS
# ============================================================

def load_all_metrics():
    metric_files = glob.glob(os.path.join(REPORTS_DIR, "metrics_v*.json"))
    data = []

    for file in metric_files:
        with open(file, "r", encoding="utf-8") as f:
            info = json.load(f)
            info["file"] = os.path.basename(file)
            data.append(info)

    if not data:
        print("⚠ No se encontraron métricas en /reports/. Ejecuta primero train.py")
        return None

    df = pd.DataFrame(data)
    df = df.sort_values("version")
    print(f"✅ {len(df)} registros de métricas cargados")
    return df

# ============================================================
# 2. CREAR /files/historial_metricas.csv
# ============================================================

def build_metric_history_csv(df):
    csv_path = os.path.join(FILES_DIR, "historial_metricas.csv")
    df.to_csv(csv_path, index=False)
    print(f"📁 Historial actualizado en: {csv_path}")

# ============================================================
# 3. GENERAR GRÁFICOS DE TENDENCIA
# ============================================================

def plot_trend(df, metric, out_path):
    if metric not in df.columns:
        print(f"⚠ La métrica '{metric}' no está en los datos")
        return

    plt.figure(figsize=(6,4))
    plt.plot(df["version"], df[metric], marker="o")
    plt.title(f"Evolución de {metric.upper()} por versión de modelo")
    plt.xlabel("Versión del modelo")
    plt.ylabel(metric.upper())
    plt.grid(True)
    plt.savefig(out_path, dpi=150)
    plt.close()
    print(f"📊 Gráfico generado: {out_path}")

# ============================================================
# 4. GENERAR REPORTE PDF CONSOLIDADO (SIEMPRE SOBREESCRIBE)
# ============================================================

def generate_consolidated_pdf(df):
    pdf_path = os.path.join(FILES_DIR, "report_evolucion_modelo.pdf")
    csv_path = os.path.join(FILES_DIR, "historial_metricas.csv")

    # Crear PDF estilo reporte formal
    pdf = FPDF()
    pdf.set_auto_page_break(auto=True, margin=10)
    pdf.add_page()

    # Título
    pdf.set_font("Arial", "B", 14)
    pdf.cell(0, 10, "Reporte de Evolucion del Modelo de Riesgo", ln=True)

    pdf.set_font("Arial", "", 10)
    pdf.multi_cell(0, 6, f"Versiones analizadas: {df['version'].tolist()}")
    pdf.multi_cell(0, 6, f"Historial guardado en: {csv_path}")
    pdf.ln(4)

    # Insertar gráficos si existen
    for metric in ["auc", "ks"]:
        img_path = os.path.join(FILES_DIR, f"metric_trend_{metric}.png")
        if os.path.exists(img_path):
            pdf.set_font("Arial", "B", 12)
            pdf.cell(0, 8, f"Evolucion de {metric.upper()}:", ln=True)
            pdf.image(img_path, w=150)
            pdf.ln(4)

    # Añadir tabla resumida
    pdf.set_font("Arial", "B", 12)
    pdf.cell(0, 8, "Resumen de metricas:", ln=True)
    pdf.set_font("Arial", "", 9)

    for _, row in df.iterrows():
        pdf.cell(0, 6, f"v{int(row['version'])} | AUC={row['auc']} | KS={row['ks']} | F1={row.get('f1')} | Fecha={row.get('created_at')}", ln=True)

    pdf.output(pdf_path)
    print(f"📄 PDF consolidado generado: {pdf_path}")

# ============================================================
# CLI / MAIN
# ============================================================

if __name__ == "__main__":
    import argparse

    parser = argparse.ArgumentParser(description="Gestión de métricas y reportes históricos.")
    parser.add_argument("--update-history", action="store_true", help="Generar historial CSV y gráficos")
    parser.add_argument("--generate-pdf", action="store_true", help="Generar reporte PDF consolidado")

    args = parser.parse_args()

    df = load_all_metrics()
    if df is None:
        exit()

    if args.update_history:
        build_metric_history_csv(df)
        # Generar gráficos de tendencias
        for metric in ["auc", "ks", "f1"]:
            out = os.path.join(FILES_DIR, f"metric_trend_{metric}.png")
            plot_trend(df, metric, out)

    if args.generate_pdf:
        generate_consolidated_pdf(df)
