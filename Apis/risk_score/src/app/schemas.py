from pydantic import BaseModel
from typing import Optional, List


# SCORING 
class ScoreRequest(BaseModel):
    monto_total: float
    cuota_inicial: float
    plazo_meses: int
    frecuencia_pago: str
    tipo_contrato: str
    mes_colocacion: int

    bien_precio: Optional[float] = None
    vehiculo_precio: Optional[float] = None

    # solo clientes existentes
    n_contratos_previos: Optional[int] = 0
    veces_con_mora_hist: Optional[int] = 0
    cierre_exitoso: Optional[int] = 0
    termino_con_deuda: Optional[int] = 0
    tuvo_mora_pero_pago: Optional[int] = 0

    # solo nuevos
    riesgo_cohorte: Optional[float] = None


class ScoreResponse(BaseModel):
    score: float
    riesgo: str
    version: int


#  INFO DE MODELO 
class ModelVersionInfo(BaseModel):
    mode: str
    version: int
    rows_train: int
    rows_valid: int
    auc: float
    ks: float
    f1: float
    precision: float
    recall: float
    decision_threshold: float
    base_rate: float
    created_at: str
    file: str


class ModelHistoryResponse(BaseModel):
    items: List[ModelVersionInfo]


class ActiveModelResponse(BaseModel):
    version: int
    mode: str
    auc: float
    ks: float
    f1: float
    precision: float
    recall: float
    created_at: str
