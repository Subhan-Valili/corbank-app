package az.corbank.abb.domain.model;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * Aggregate root for a submitted payment file. Unlike the other, purely descriptive domain
 * records, this one has real identity (batchNumber) and a lifecycle — it's created the
 * moment ABB accepts a submission, in status SUBMITTED, and moves through IN_PROGRESS /
 * PARTIAL towards a settled COMPLETED / FAILURE / ERROR as it gets polled. That lifecycle
 * transition is genuine domain behavior, which is why this is a class with behavior rather
 * than a plain record like the rest of this package.
 */
public class PaymentBatch {

    private final String batchNumber;
    private final String externalReference;
    private final BatchType type;
    private final Instant submittedAt;

    private BatchStatusCode status;
    private String statusDescription;
    private Instant lastCheckedAt;
    private List<PaymentLine> payments;

    /** Freshly accepted by ABB — no status poll has happened yet. */
    public static PaymentBatch newlySubmitted(String batchNumber, String externalReference, BatchType type) {
        return new PaymentBatch(batchNumber, externalReference, type, Instant.now(),
                BatchStatusCode.SUBMITTED, null, null, List.of());
    }

    /** Reconstructs a batch from persistence — used by the persistence adapter, not by application logic. */
    public static PaymentBatch reconstitute(String batchNumber, String externalReference, BatchType type,
                                              Instant submittedAt, BatchStatusCode status, String statusDescription,
                                              Instant lastCheckedAt) {
        return new PaymentBatch(batchNumber, externalReference, type, submittedAt,
                status, statusDescription, lastCheckedAt, List.of());
    }

    private PaymentBatch(String batchNumber, String externalReference, BatchType type, Instant submittedAt,
                          BatchStatusCode status, String statusDescription, Instant lastCheckedAt,
                          List<PaymentLine> payments) {
        this.batchNumber = Objects.requireNonNull(batchNumber);
        this.externalReference = externalReference;
        this.type = Objects.requireNonNull(type);
        this.submittedAt = Objects.requireNonNull(submittedAt);
        this.status = Objects.requireNonNull(status);
        this.statusDescription = statusDescription;
        this.lastCheckedAt = lastCheckedAt;
        this.payments = payments;
    }

    /** Applies a fresh status read from ABB (spec §4.7/§4.13) — the batch's one real state transition. */
    public void applyRemoteStatus(BatchStatusCode status, String statusDescription, List<PaymentLine> payments) {
        this.status = Objects.requireNonNull(status);
        this.statusDescription = statusDescription;
        this.payments = payments != null ? payments : List.of();
        this.lastCheckedAt = Instant.now();
    }

    public String batchNumber() { return batchNumber; }
    public String externalReference() { return externalReference; }
    public BatchType type() { return type; }
    public Instant submittedAt() { return submittedAt; }
    public BatchStatusCode status() { return status; }
    public String statusDescription() { return statusDescription; }
    public Instant lastCheckedAt() { return lastCheckedAt; }
    public List<PaymentLine> payments() { return payments; }
}
