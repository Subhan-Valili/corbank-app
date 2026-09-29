package az.corbank.mscorbank.application.service;

import az.corbank.mscorbank.domain.model.Bank;
import az.corbank.mscorbank.domain.model.PaymentCommand;
import az.corbank.mscorbank.domain.model.PaymentResult;
import az.corbank.mscorbank.application.port.in.MakePaymentUseCase;
import org.springframework.stereotype.Service;

@Service
class PaymentService implements MakePaymentUseCase {

    private final BankGateways banks;

    PaymentService(BankGateways banks) {
        this.banks = banks;
    }

    /** The bank that owns the source account executes the payment; ABB is recognised first (see AccountService). */
    @Override
    public PaymentResult pay(PaymentCommand command) {
        boolean abbAccount = banks.findAccountSafely(Bank.ABB, command.fromAccountId()).isPresent();
        return banks.require(abbAccount ? Bank.ABB : Bank.PASHA).submitPayment(command);
    }
}
