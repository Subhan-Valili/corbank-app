package az.corbank.mscorbank.domain.model;

/** A bank that actually has accounts for the current customer. */
public record BankConnection(Bank bank, int accountCount) {
}
