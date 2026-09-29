package az.corbank.mscorbank.application.port.in;

import az.corbank.mscorbank.domain.model.PaymentCommand;
import az.corbank.mscorbank.domain.model.PaymentResult;

public interface MakePaymentUseCase {

    PaymentResult pay(PaymentCommand command);
}
