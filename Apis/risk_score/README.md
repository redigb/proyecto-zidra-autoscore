## 📘 README --- Motor de Riesgo Zidra_RD

### Sistema de Scoring Crediticio Interno --- Entrenamiento + Scoring + Versionado

## 🟩 1. Descripción General

Este proyecto implementa un motor de riesgo interno que:

-   Entrena modelos de Machine Learning con datos históricos.
-   Versiona automáticamente cada modelo entrenado.
-   Realiza scoring en tiempo real para clientes nuevos y existentes.
-   Genera métricas, reportes históricos y gráficos automáticos.
-   Se conecta a PocketBase para obtener contratos, pagos y recibos.

El sistema está compuesto por un pipeline de ML y una API REST
construida con FastAPI.

## 🟦 2. Endpoints principales

### ▶ POST /score

Genera el score de riesgo para un cliente.

#### Ejemplo de entrada:

``` json
{
  "monto_total": 5000,
  "cuota_inicial": 1000,
  "plazo_meses": 12,
  "frecuencia_pago": "mensual",
  "tipo_contrato": "credito_personal",
  "mes_colocacion": 7,
  "bien_precio": 5500,
  "vehiculo_precio": 0,
  "n_contratos_previos": 0,
  "veces_con_mora_hist": 0,
  "cierre_exitoso": 0,
  "termino_con_deuda": 0,
  "tuvo_mora_pero_pago": 0,
  "riesgo_cohorte": 0.18
}
```

#### Respuesta típica:

``` json
{
  "score": 0.742,
  "riesgo": "ALTO",
  "version": 4
}
```

### ▶ GET /models/active

Devuelve la información del modelo actualmente activo.

``` json
{
  "active_version": 9,
  "auc": 0.83,
  "ks": 0.54,
  "base_rate": 0.62
}
```

### ▶ POST /models/train

Ejecuta el pipeline completo:

-   Extrae datos desde PocketBase
-   Genera dataset ML
-   Preprocesa columnas
-   Entrena un modelo nuevo
-   Guarda métricas
-   Actualiza registro de versiones

#### Respuesta:

``` json
{
  "status": "training_pipeline_completed",
  "active_model": { "version": 9 }
}
```

## 🟩 3. Flujo interno del modelo

Pipeline en orden:

1.  data_source_api.py → Carga datos desde PocketBase\
2.  data_prep.py → Limpieza + dataset_ml_final.parquet\
3.  preprocess.py → transformer.pkl\
4.  train.py → model_vX.pkl + métricas\
5.  metrics.py → historial + PDF consolidado\
6.  versioning.py → controla modelo activo/reserva

Carpetas generadas automáticamente:

    /data → dataset + transformer
    /models → versiones del modelo
    /reports → métricas por versión
    /files → historial y reportes

## 🟦 4. Features utilizadas por el modelo

### 🟩 Clientes Nuevos

-   tipo_contrato\
-   monto_total\
-   cuota_inicial\
-   plazo_meses\
-   frecuencia_pago\
-   mes_colocacion\
-   agente_acreditario\
-   bien_precio / vehiculo_precio\
-   riesgo_cohorte

### 🟦 Clientes Existentes

-   dpd_max_6m\
-   dpd_prom_6m\
-   pagos_realizados_6m\
-   pagos_esperados_6m\
-   ratio_pago_cuotas_6m\
-   dias_desde_ultimo_pago\
-   veces_con_mora_hist\
-   n_contratos_previos\
-   cierre_exitoso\
-   termino_con_deuda

## 🟧 5. Límites recomendados de datos

-   Mínimo: 1000 registros\
-   Máximo: 2000 registros por entrenamiento

## 🟦 6. Arranque de la API

### Modo desarrollo:

``` bash
uvicorn src.app.main:app --reload
```

### Modo servidor:

``` bash
uvicorn src.app.main:app --host 0.0.0.0 --port 8000
```

## 🟥 7. Problemas comunes

❌ "No existe transformer"\
→ Ejecutar /models/train.

❌ "model_registry.json no existe"\
→ Aún no se ha entrenado el modelo inicial.

❌ Score alto en clientes nuevos\
→ No usar riesgo_cohorte = 0\
→ Usar valores reales entre 0.10 y 0.30.
