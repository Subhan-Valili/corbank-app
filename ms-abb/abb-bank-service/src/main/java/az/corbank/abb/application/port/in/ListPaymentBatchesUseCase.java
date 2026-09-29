package az.corbank.abb.application.port.in;

import az.corbank.abb.domain.model.PaymentBatch;

import java.util.List;

public interface ListPaymentBatchesUseCase {
    /** Every batch we've ever submitted, newest first — served entirely from our own database. */
    List<PaymentBatch> listBatches();
}
