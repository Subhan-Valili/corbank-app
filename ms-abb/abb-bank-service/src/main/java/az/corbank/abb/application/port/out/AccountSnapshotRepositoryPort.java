package az.corbank.abb.application.port.out;

import az.corbank.abb.domain.model.AccountBalance;

import java.util.Optional;

public interface AccountSnapshotRepositoryPort {

    void save(AccountBalance balance);

    Optional<AccountBalance> findByAccountNumber(String accountNumber);
}
