package az.corbank.abb.application.port.in;

import az.corbank.abb.domain.model.PaymentLine;

public interface GetIndividualPaymentUseCase {
    /** spec §4.8. */
    PaymentLine getPayment(String batchNumber, String paymentId);
}
