package az.corbank.abb.domain.model;

/**
 * Input value object for fetching a statement — spec §4.10. fromDate/toDate are expected
 * pre-formatted as YYYYMMDD (the wire format ABB wants); it's the driving adapter's job to
 * turn whatever date format the caller used into that before this reaches the domain.
 */
public record StatementQuery(
        String accountNumber,
        String fromDate,
        String toDate,
        int page,
        int pageSize,
        OperationType operationType
) {
    public enum OperationType {
        ALL, DEBIT, CREDIT
    }
}
