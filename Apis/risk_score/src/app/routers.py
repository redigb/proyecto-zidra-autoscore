from fastapi import APIRouter, HTTPException

from src.app.schemas import (
    ScoreRequest, ScoreResponse,
    ModelHistoryResponse, ModelVersionInfo,
    ActiveModelResponse,
)
from src.app.model_service import model_service

import pandas as pd
import subprocess
import sys

router = APIRouter()



@router.post("/score", response_model=ScoreResponse)
def score_endpoint(payload: ScoreRequest):
    model_service.ensure_loaded()
    result = model_service.predict(payload.dict())
    return ScoreResponse(**result)


@router.post("/models/train")
def train_full_pipeline():
    commands = [
        [sys.executable, "-m", "src.ml.data_source_api"],
        [sys.executable, "-m", "src.ml.data_prep"],
        [sys.executable, "-m", "src.pipeline.preprocess"],
        [sys.executable, "-m", "src.ml.train", "--mode", "full"],
        [sys.executable, "-m", "src.ml.metrics", "--update-history"],
    ]

    logs = []

    for cmd in commands:
        try:
            result = subprocess.run(
                cmd,
                check=True,
                capture_output=True,
                text=True,
            )
            logs.append({
                "command": " ".join(cmd),
                "stdout": result.stdout[-500:],
            })
        except subprocess.CalledProcessError as e:
            error_msg = e.stderr[-500:] if e.stderr else str(e)
            raise HTTPException(
                status_code=500,
                detail={
                    "error": f"Error ejecutando: {' '.join(cmd)}",
                    "stderr": error_msg,
                },
            )

    # 🔄 RECARGAR MODELO Y REGISTRO DESPUÉS DEL ENTRENAMIENTO
    try:
        model_service.reload_active_model()
    except Exception as e:
        print("Error recargando modelo:", e)

    # Obtener info del modelo activo actualizado
    try:
        active_info = model_service.get_active_model_info()
    except Exception:
        active_info = None

    return {
        "status": "training_pipeline_completed",
        "steps": logs,
        "active_model": active_info
    }



@router.get("/models/history", response_model=ModelHistoryResponse)
def get_models_history():
    df = model_service.load_metrics_history()
    records = df.to_dict(orient="records")

    items = [ModelVersionInfo(**row) for row in records]
    return ModelHistoryResponse(items=items)


@router.get("/models/active", response_model=ActiveModelResponse)
def get_active_model():
    info = model_service.get_active_model_info()
    return ActiveModelResponse(**info)


@router.get("/models/status")
def model_status():
    if model_service.model is None:
        return {
            "status": "NO_MODEL",
            "message": "Ejecute /models/train para crear el primer modelo."
        }
    else:
        return {
            "status": "READY",
            "active_version": model_service.active_version
        }