package az.corbank.abb.adapter.out.persistence;

import az.corbank.abb.application.port.out.AccountSnapshotRepositoryPort;
import az.corbank.abb.domain.model.AccountBalance;
import az.corbank.abb.domain.model.DataOrigin;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
class AccountSnapshotPersistenceAdapter implements AccountSnapshotRepositoryPort {

    private final AccountSnapshotJpaRepository jpaRepository;

    AccountSnapshotPersistenceAdapter(AccountSnapshotJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    @Transactional
    public void save(AccountBalance balance) {
        AccountSnapshotJpaEntity entity = jpaRepository.findById(balance.accountNumber())
                .orElseGet(AccountSnapshotJpaEntity::new);
        entity.setAccountNumber(balance.accountNumber());
        entity.setCurrency(balance.currency());
        entity.setAvailableBalance(balance.availableBalance());
        entity.setFetchedAt(balance.fetchedAt());
        jpaRepository.save(entity);
    }

    @Override
    public Optional<AccountBalance> findByAccountNumber(String accountNumber) {
        return jpaRepository.findById(accountNumber).map(e -> new AccountBalance(
                e.getAccountNumber(), e.getCurrency(), e.getAvailableBalance(), e.getFetchedAt(),
                DataOrigin.SNAPSHOT_FALLBACK));
    }
}
