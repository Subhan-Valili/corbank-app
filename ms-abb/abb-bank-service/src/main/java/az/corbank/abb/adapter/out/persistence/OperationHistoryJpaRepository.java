package az.corbank.abb.adapter.out.persistence;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

interface OperationHistoryJpaRepository extends JpaRepository<OperationHistoryJpaEntity, Long> {

    Optional<OperationHistoryJpaEntity> findByAccountNumberAndReference(String accountNumber, String reference);

    List<OperationHistoryJpaEntity> findByAccountNumberOrderByTransactionDateDescIdDesc(String accountNumber, Pageable pageable);

    List<OperationHistoryJpaEntity> findAllByOrderByTransactionDateDescIdDesc(Pageable pageable);
}
