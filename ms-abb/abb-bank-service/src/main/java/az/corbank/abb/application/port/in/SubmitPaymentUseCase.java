package az.corbank.abb.application.port.in;

import az.corbank.abb.domain.model.PaymentInstruction;
import az.corbank.abb.domain.model.PaymentOutcome;

public interface SubmitPaymentUseCase {
    PaymentOutcome submit(PaymentInstruction instruction);
}
