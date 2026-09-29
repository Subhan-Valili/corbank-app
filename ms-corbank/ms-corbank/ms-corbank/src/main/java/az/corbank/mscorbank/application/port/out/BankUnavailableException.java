package az.corbank.mscorbank.application.port.out;

import az.corbank.mscorbank.domain.model.Bank;

/** A bank could not be reached or answered with a technical error. Thrown by BankGateway adapters. */
public class BankUnavailableException extends RuntimeException {

    private final Bank bank;

    public BankUnavailableException(Bank bank, String message, Throwable cause) {
        super(message, cause);
        this.bank = bank;
    }

    public Bank bank() {
        return bank;
    }
}
