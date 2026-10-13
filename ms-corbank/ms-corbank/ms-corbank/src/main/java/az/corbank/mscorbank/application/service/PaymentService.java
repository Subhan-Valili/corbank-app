package az.corbank.mscorbank.application.service;

import az.corbank.mscorbank.application.exception.AccountNotFoundException;
import az.corbank.mscorbank.domain.model.Account;
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

    /** Ödəniş yalnız cari istifadəçinin ÖZ hesabından olur; hesabın sahibi olan bank icra edir. */
    @Override
    public PaymentResult pay(PaymentCommand command) {
        Account from = banks.findOwnedAccount(command.fromAccountId())
                .orElseThrow(() -> new AccountNotFoundException(command.fromAccountId()));
        return banks.require(from.bank()).submitPayment(command);
    }
}
