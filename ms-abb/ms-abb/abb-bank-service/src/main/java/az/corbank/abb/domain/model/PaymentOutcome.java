package az.corbank.abb.domain.model;

public record PaymentOutcome(
        boolean completed,
        String message,
        String reference   // ABB's batchNumber (or the mock gateway's fabricated equivalent), when completed
) {
    public static PaymentOutcome completed(String reference) {
        return new PaymentOutcome(true, "Ödəniş qəbul edildi.", reference);
    }

    public static PaymentOutcome rejected(String message) {
        return new PaymentOutcome(false, message, null);
    }
}
