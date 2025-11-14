import 'package:flutter/material.dart';
import '../models/credit_request.dart';
import '../services/ai_service.dart';

class CreditController extends ChangeNotifier {
  final AIService aiService;

  CreditController(this.aiService);

  double risk = 0.0;

  void calculateRisk(CreditRequest request) {
    risk = aiService.evaluateRisk(request);
    notifyListeners();
  }
}