package az.corbank.abb.domain.model;

/** spec §4.6. */
public record FileStatus(
        String externalReference,
        String batchNumber,
        BatchStatusCode status,
        String description
) {
}
