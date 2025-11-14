import 'package:flutter/material.dart';

class CreditRequestScreen extends StatefulWidget {
  const CreditRequestScreen({super.key});

  @override
  State<CreditRequestScreen> createState() => _CreditRequestScreenState();
}

class _CreditRequestScreenState extends State<CreditRequestScreen> {
  final _amountController = TextEditingController();
  final _installmentsController = TextEditingController();
  String? _selectedFrequency;

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Solicitud de crédito')),
      body: Padding(
        padding: const EdgeInsets.all(16),
        child: ListView(
          children: [
            TextField(
              controller: _amountController,
              decoration: const InputDecoration(
                labelText: 'Monto solicitado',
                hintText: 'Ej: S/ 5,000',
              ),
              keyboardType: TextInputType.number,
            ),
            const SizedBox(height: 12),
            TextField(
              controller: _installmentsController,
              decoration: const InputDecoration(
                labelText: 'Número de cuotas',
                hintText: 'Ej: 12',
              ),
              keyboardType: TextInputType.number,
            ),
            const SizedBox(height: 12),
            DropdownButtonFormField<String>(
              value: _selectedFrequency,
              items: const [
                DropdownMenuItem(value: 'Mensual', child: Text('Mensual')),
                DropdownMenuItem(value: 'Quincenal', child: Text('Quincenal')),
              ],
              decoration: const InputDecoration(labelText: 'Frecuencia de pago'),
              onChanged: (v) => setState(() => _selectedFrequency = v),
            ),
            const SizedBox(height: 24),
            const Text('Documentos'),
            const SizedBox(height: 8),
            _buildUploadTile('Foto de DNI'),
            _buildUploadTile('Recibo de luz/agua'),
            _buildUploadTile('Selfie con DNI'),
            const SizedBox(height: 24),
            ElevatedButton(
              onPressed: () {
                ScaffoldMessenger.of(context).showSnackBar(
                  const SnackBar(content: Text('Solicitud enviada')),
                );
              },
              child: const Text('Enviar solicitud'),
            )
          ],
        ),
      ),
    );
  }

  Widget _buildUploadTile(String label) => ListTile(
        title: Text(label),
        trailing: const Icon(Icons.upload_file, color: Colors.blue),
      );
}
