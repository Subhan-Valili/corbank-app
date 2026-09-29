package az.corbank.abb.domain.exception;

public class PaymentBatchNotFoundException extends RuntimeException {
    public PaymentBatchNotFoundException(String batchNumber) {
        super("Unknown batch: " + batchNumber);
    }
}
