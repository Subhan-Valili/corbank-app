package az.corbank.abb.adapter.out.persistence;

import az.corbank.abb.application.port.out.PaymentBatchRepositoryPort;
import az.corbank.abb.domain.model.BatchStatusCode;
import az.corbank.abb.domain.model.BatchType;
import az.corbank.abb.domain.model.PaymentBatch;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Implements the PaymentBatchRepositoryPort against JPA/Postgres. This is the only class
 * that knows both the PaymentBatch domain aggregate AND the PaymentBatchJpaEntity shape —
 * the mapping between them lives here and nowhere else.
 */
@Component
class PaymentBatchPersistenceAdapter implements PaymentBatchRepositoryPort {

    private final PaymentBatchJpaRepository jpaRepository;

    PaymentBatchPersistenceAdapter(PaymentBatchJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    @Transactional
    public PaymentBatch save(PaymentBatch batch) {
        PaymentBatchJpaEntity entity = jpaRepository.findByBatchNumber(batch.batchNumber())
                .orElseGet(PaymentBatchJpaEntity::new);

        entity.setBatchNumber(batch.batchNumber());
        entity.setExternalReference(batch.externalReference());
        entity.setBatchType(batch.type().name());
        entity.setLastKnownStatus(batch.status().name());
        entity.setLastKnownStatusDescription(batch.statusDescription());
        entity.setSubmittedAt(batch.submittedAt());
        entity.setLastCheckedAt(batch.lastCheckedAt());

        jpaRepository.save(entity);
        return batch; // the aggregate itself is already up to date; we just persisted its state
    }

    @Override
    public Optional<PaymentBatch> findByBatchNumber(String batchNumber) {
        return jpaRepository.findByBatchNumber(batchNumber).map(this::toDomain);
    }

    @Override
    public List<PaymentBatch> findAllOrderBySubmittedAtDesc() {
        return jpaRepository.findAllByOrderBySubmittedAtDesc().stream().map(this::toDomain).toList();
    }

    private PaymentBatch toDomain(PaymentBatchJpaEntity entity) {
        PaymentBatch batch = PaymentBatch.reconstitute(
                entity.getBatchNumber(),
                entity.getExternalReference(),
                BatchType.valueOf(entity.getBatchType()),
                entity.getSubmittedAt(),
                BatchStatusCode.valueOf(entity.getLastKnownStatus()),
                entity.getLastKnownStatusDescription(),
                entity.getLastCheckedAt());
        return batch;
    }
}
