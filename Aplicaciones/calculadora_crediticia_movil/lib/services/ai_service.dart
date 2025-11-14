import '../models/credit_request.dart';

class AIService {
  double evaluateRisk(CreditRequest request) {
    // Simulación simple de análisis de riesgo
    if (request.amount < 3000) return 0.2;
    if (request.amount < 10000) return 0.35;
    return 0.6;
  }
}