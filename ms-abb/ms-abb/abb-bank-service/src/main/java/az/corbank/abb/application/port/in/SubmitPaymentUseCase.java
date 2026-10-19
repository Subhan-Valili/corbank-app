package az.corbank.abb.application.port.in;

import az.corbank.abb.domain.model.PaymentInstruction;
import az.corbank.abb.domain.model.CustomerIdentity;
import az.corbank.abb.domain.model.PaymentOutcome;

public interface SubmitPaymentUseCase {
    PaymentOutcome submit(CustomerIdentity identity, PaymentInstruction instruction);
}
