from fastapi import FastAPI
from src.app.routers import router

app = FastAPI(
    title="Risk Score API",
    description="Servicio de scoring de riesgo",
    version="1.0"
)

app.include_router(router, prefix="/api")

@app.get("/")
def root():
    return {"msg": "Risk Score API funcionando"}
