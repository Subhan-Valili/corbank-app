package az.corbank.abb.domain.model;

import java.math.BigDecimal;

/**
 * One transaction line within an AccountStatement — spec §4.10. amount is always positive.
 * Carries its own currency (rather than relying on the parent statement's) because lines
 * are also the unit persisted into operation history (see OperationHistoryRepositoryPort),
 * where they're stored and queried independently of any single statement fetch.
 */
public record StatementLine(
        String accountNumber,
        String reference,
        String date,          // dd.MM.yyyy, as ABB reports it
        String description,
        String counterparty,
        String beneficiaryTin,
        BigDecimal amount,
        String currency,
        Direction direction
) {
}
