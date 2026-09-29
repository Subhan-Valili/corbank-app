package az.ingress.mspashabank.repository;

import az.ingress.mspashabank.entity.PashaAccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface PashaBankAccountRepository extends JpaRepository<PashaAccountEntity, Long> {

    @Query("SELECT pa FROM PashaAccountEntity pa WHERE pa.customerNo = :customerNo")
    List<PashaAccountEntity> findAccountsByCustomerNo(@Param("customerNo") String customerNo);

    @Query("SELECT pa FROM PashaAccountEntity pa WHERE pa.accountId = :accountId")
    Optional<PashaAccountEntity> findAccountByAccountId(@Param("accountId") String accountId);

    @Query("SELECT pa FROM PashaAccountEntity pa WHERE pa.iban = :iban")
    Optional<PashaAccountEntity> findAccountByIban(@Param("iban") String iban);

    @Query("""
            SELECT pa FROM PashaAccountEntity pa
            WHERE pa.accountId IN :identifiers OR pa.iban IN :identifiers
            """)
    List<PashaAccountEntity> findAccountsByAccountIdInOrIbanIn(@Param("identifiers") Collection<String> identifiers);
}