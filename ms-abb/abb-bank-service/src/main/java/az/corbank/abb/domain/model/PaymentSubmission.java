package az.corbank.abb.domain.model;

/** What's needed to submit a payment file — spec §4.2-4.4, §4.11-4.12. */
public record PaymentSubmission(
        BatchType type,
        String base64aDoc,
        String externalReference
) {
}
