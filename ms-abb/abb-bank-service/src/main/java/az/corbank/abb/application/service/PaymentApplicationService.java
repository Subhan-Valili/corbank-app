package az.corbank.abb.application.service;

import az.corbank.abb.application.port.in.*;
import az.corbank.abb.application.port.out.AbbBankGateway;
import az.corbank.abb.application.port.out.PaymentBatchRepositoryPort;
import az.corbank.abb.domain.exception.PaymentBatchNotFoundException;
import az.corbank.abb.domain.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Orchestrates AbbBankGateway (out port) and PaymentBatchRepositoryPort (out port) for
 * every payment-related use case. Two pieces of real domain behavior live here:
 *   - submit(): a batch is a genuinely new aggregate the moment ABB accepts it, so it's
 *     created and persisted here, not just passed through.
 *   - getBatchStatus(): applies ABB's latest status onto the existing PaymentBatch
 *     aggregate (PaymentBatch.applyRemoteStatus()) and re-persists it — the aggregate
 *     owns that state transition, this service just drives it.
 */
public class PaymentApplicationService implements
        SubmitPaymentUseCase, VerifyOtpUseCase, GetBatchStatusUseCase,
        GetIndividualPaymentUseCase, GetFileStatusUseCase, ListPaymentBatchesUseCase {

    private static final Logger log = LoggerFactory.getLogger(PaymentApplicationService.class);

    private final AbbBankGateway gateway;
    private final PaymentBatchRepositoryPort batchRepository;

    public PaymentApplicationService(AbbBankGateway gateway, PaymentBatchRepositoryPort batchRepository) {
        this.gateway = gateway;
        this.batchRepository = batchRepository;
    }

    @Override
    public PaymentBatch submit(PaymentSubmission submission) {
        String batchNumber = gateway.submitPayment(submission);
        PaymentBatch batch = PaymentBatch.newlySubmitted(batchNumber, submission.externalReference(), submission.type());
        batchRepository.save(batch);
        log.info("Submitted {} payment batch {}", submission.type(), batchNumber);
        return batch;
    }

    @Override
    public String verifyOtp(String batchNumber, String otpCode) {
        return gateway.verifyOtp(batchNumber, otpCode);
    }

    @Override
    public PaymentBatch getBatchStatus(String batchNumber) {
        PaymentBatch batch = batchRepository.findByBatchNumber(batchNumber)
                .orElseThrow(() -> new PaymentBatchNotFoundException(batchNumber));

        AbbBankGateway.BatchStatusResult result = gateway.getBatchStatus(batchNumber, batch.type().isSalary());
        batch.applyRemoteStatus(result.status(), result.statusDescription(), result.payments());
        return batchRepository.save(batch);
    }

    @Override
    public PaymentLine getPayment(String batchNumber, String paymentId) {
        return gateway.getIndividualPayment(batchNumber, paymentId);
    }

    @Override
    public FileStatus getFileStatus(String externalReference) {
        return gateway.getFileStatus(externalReference);
    }

    @Override
    public List<PaymentBatch> listBatches() {
        return batchRepository.findAllOrderBySubmittedAtDesc();
    }
}
