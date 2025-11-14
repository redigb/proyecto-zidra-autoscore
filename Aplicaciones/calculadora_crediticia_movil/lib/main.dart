import 'package:flutter/material.dart';
import 'package:provider/provider.dart';

// Controladores
import 'package:calculadora_crediticia_movil/controllers/auth_controller.dart';
import 'package:calculadora_crediticia_movil/controllers/credit_controller.dart';

// Servicios
import 'package:calculadora_crediticia_movil/services/auth_service.dart';
import 'package:calculadora_crediticia_movil/services/ai_service.dart';

// Pantallas base
import 'package:calculadora_crediticia_movil/views/screens/home_screen.dart';
import 'package:calculadora_crediticia_movil/views/screens/login_screen.dart';
import 'package:calculadora_crediticia_movil/views/screens/dashboard_screen.dart';

// 👇 Nuevas pantallas agregadas
import 'package:calculadora_crediticia_movil/views/screens/credit_request_screen.dart';
import 'package:calculadora_crediticia_movil/views/screens/credit_status_screen.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MultiProvider(
      providers: [
        ChangeNotifierProvider(
          create: (context) => AuthController(AuthService()),
        ),
        ChangeNotifierProvider(
          create: (context) => CreditController(AIService()),
        ),
      ],
      child: MaterialApp(
        title: 'Cipin',
        debugShowCheckedModeBanner: false,
        theme: ThemeData(
          colorScheme: ColorScheme.fromSeed(seedColor: Colors.blueAccent),
          useMaterial3: true,
        ),
        initialRoute: '/',
        routes: {
          '/': (context) => HomeScreen(),
          '/login': (context) => LoginScreen(),
          '/dashboard': (context) => DashboardScreen(),

          // 👇 RUTAS NUEVAS
          '/solicitud': (_) => const CreditRequestScreen(),
          '/estado': (_) => const CreditStatusScreen(),
        },
      ),
    );
  }
}
