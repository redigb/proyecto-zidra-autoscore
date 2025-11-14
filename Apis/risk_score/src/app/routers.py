from fastapi import APIRouter
from src.app.schemas import ScoreRequest, ScoreResponse
from src.app.model_service import model_service

router = APIRouter()

@router.post("/score", response_model=ScoreResponse)
def score_endpoint(payload: ScoreRequest):
    result = model_service.predict(payload.dict())
    return ScoreResponse(**result)
