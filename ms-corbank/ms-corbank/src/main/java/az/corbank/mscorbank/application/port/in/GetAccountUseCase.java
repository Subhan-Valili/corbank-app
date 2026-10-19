package az.corbank.mscorbank.application.port.in;

import az.corbank.mscorbank.domain.model.Account;

import java.util.Optional;

public interface GetAccountUseCase {

    Optional<Account> getAccount(String accountId);
}
