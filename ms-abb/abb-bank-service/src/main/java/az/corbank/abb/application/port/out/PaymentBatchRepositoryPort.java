package az.corbank.abb.application.port.out;

import az.corbank.abb.domain.model.PaymentBatch;

import java.util.List;
import java.util.Optional;

public interface PaymentBatchRepositoryPort {

    PaymentBatch save(PaymentBatch batch);

    Optional<PaymentBatch> findByBatchNumber(String batchNumber);

    List<PaymentBatch> findAllOrderBySubmittedAtDesc();
}
