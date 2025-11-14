import 'package:flutter/material.dart';

class DashboardScreen extends StatelessWidget {
  const DashboardScreen({super.key});

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Panel Principal')),
      body: ListView(
        padding: const EdgeInsets.all(20),
        children: [
          const Text(
            'Bienvenido al Dashboard',
            style: TextStyle(fontSize: 18, fontWeight: FontWeight.bold),
          ),
          const SizedBox(height: 20),
          ElevatedButton(
            onPressed: () => Navigator.pushNamed(context, '/solicitud'),
            child: const Text('Nueva Solicitud de Crédito'),
          ),
          const SizedBox(height: 20),
          ElevatedButton(
            onPressed: () => Navigator.pushNamed(context, '/estado'),
            child: const Text('Ver Estado de Solicitud'),
          ),
        ],
      ),
    );
  }
}
