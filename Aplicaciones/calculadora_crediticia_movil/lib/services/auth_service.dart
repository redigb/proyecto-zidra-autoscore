class AuthService {
  bool login(String username, String password) {
    // Simulación simple de inicio de sesión
    return username == 'admin' && password == '1234';
  }
}
