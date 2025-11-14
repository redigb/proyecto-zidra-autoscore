import 'package:flutter/material.dart';
import '../services/auth_service.dart';

class AuthController extends ChangeNotifier {
  final AuthService authService;

  AuthController(this.authService);

  bool _isLoggedIn = false;

  bool get isLoggedIn => _isLoggedIn;

  void login(String username, String password) {
    _isLoggedIn = authService.login(username, password);
    notifyListeners();
  }

  void logout() {
    _isLoggedIn = false;
    notifyListeners();
  }
}
