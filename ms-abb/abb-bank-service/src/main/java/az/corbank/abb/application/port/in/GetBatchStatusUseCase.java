package az.corbank.abb.application.port.in;

import az.corbank.abb.domain.model.PaymentBatch;

public interface GetBatchStatusUseCase {
    /** Refreshes the batch's status from ABB and persists the update — spec §4.7/§4.13. */
    PaymentBatch getBatchStatus(String batchNumber);
}
