package az.corbank.mscorbank.adapter.out.persistence.repository.jpa;

import az.corbank.mscorbank.adapter.out.persistence.entity.BankApiKeyEntity;
import az.corbank.mscorbank.domain.model.Bank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BankApiKeyRepository extends JpaRepository<BankApiKeyEntity, Long> {

    Optional<BankApiKeyEntity> findByUser_EmailAndBank(String email, Bank bank);

    List<BankApiKeyEntity> findAllByUser_Email(String email);

    long deleteByUser_EmailAndBank(String email, Bank bank);
}
