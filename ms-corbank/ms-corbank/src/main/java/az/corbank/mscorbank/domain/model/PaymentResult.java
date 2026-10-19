package az.corbank.mscorbank.domain.model;

public record PaymentResult(PaymentStatus status, String message, PaymentSource source, Long bulkId) {

    public static PaymentResult completed(String message, Long bulkId) {
        return new PaymentResult(PaymentStatus.COMPLETED, message, PaymentSource.LIVE, bulkId);
    }

    public static PaymentResult rejected(String message) {
        return new PaymentResult(PaymentStatus.REJECTED, message, PaymentSource.MOCK, null);
    }
}
