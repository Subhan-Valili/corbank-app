package az.corbank.abb.application.port.in;

import az.corbank.abb.domain.model.AccountBalance;

public interface GetAccountBalanceUseCase {
    AccountBalance getBalance(String accountNumber);
}
