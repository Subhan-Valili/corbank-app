package az.corbank.abb.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

interface PaymentBatchJpaRepository extends JpaRepository<PaymentBatchJpaEntity, Long> {

    Optional<PaymentBatchJpaEntity> findByBatchNumber(String batchNumber);

    List<PaymentBatchJpaEntity> findAllByOrderBySubmittedAtDesc();
}
