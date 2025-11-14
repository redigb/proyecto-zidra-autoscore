import 'package:flutter/material.dart';

class CreditStatusScreen extends StatelessWidget {
  const CreditStatusScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Estado de tu solicitud')),
      body: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Text('Crédito Personal',
                style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold)),
            const SizedBox(height: 6),
            const Text('Última actualización: 20 de mayo de 2024'),
            const SizedBox(height: 20),
            const Text('Análisis de Riesgo',
                style: TextStyle(fontSize: 16, fontWeight: FontWeight.bold)),
            const SizedBox(height: 8),
            Container(
              padding: const EdgeInsets.all(16),
              decoration: BoxDecoration(
                color: Colors.green.withOpacity(0.1),
                borderRadius: BorderRadius.circular(10),
              ),
              child: Row(
                children: const [
                  Text('25%',
                      style: TextStyle(
                          fontSize: 32,
                          fontWeight: FontWeight.bold,
                          color: Colors.green)),
                  SizedBox(width: 16),
                  Expanded(
                    child: Text(
                      'Riesgo Bajo\nTu scoring de riesgo es favorable.',
                      style: TextStyle(fontSize: 16),
                    ),
                  ),
                ],
              ),
            ),
            const SizedBox(height: 24),
            ListTile(
              leading: const Icon(Icons.history),
              title: const Text('Historial de la solicitud'),
              onTap: () {},
            ),
            ListTile(
              leading: const Icon(Icons.insights),
              title: const Text('Detalles de la interpretación'),
              onTap: () {},
            ),
          ],
        ),
      ),
    );
  }
}
