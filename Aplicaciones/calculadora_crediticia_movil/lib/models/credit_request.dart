class CreditRequest {
  final double amount;
  final int installments;
  final String frequency;

  CreditRequest({
    required this.amount,
    required this.installments,
    required this.frequency,
  });
}