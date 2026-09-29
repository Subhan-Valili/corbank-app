package az.corbank.abb.application.port.in;

import az.corbank.abb.domain.model.PaymentBatch;
import az.corbank.abb.domain.model.PaymentSubmission;

public interface SubmitPaymentUseCase {
    PaymentBatch submit(PaymentSubmission submission);
}
