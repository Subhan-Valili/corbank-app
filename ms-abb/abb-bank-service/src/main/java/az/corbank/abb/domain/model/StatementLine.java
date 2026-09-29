package az.corbank.abb.domain.model;

import java.math.BigDecimal;

/** One transaction line within an AccountStatement — spec §4.10. amount is always positive. */
public record StatementLine(
        String reference,
        String date,          // dd.MM.yyyy, as ABB reports it
        String description,
        String counterparty,
        String beneficiaryTin,
        BigDecimal amount,
        Direction direction
) {
}
