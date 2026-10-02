package az.corbank.abb.adapter.in.web.dto;

import az.corbank.abb.domain.model.StatementLine;

import java.math.BigDecimal;

public record OperationLineResponse(
        String accountNumber,
        String reference,
        String date,
        String description,
        String counterparty,
        String beneficiaryTin,
        BigDecimal amount,
        String currency,
        String direction   // "IN" | "OUT"
) {
    public static OperationLineResponse from(StatementLine l) {
        return new OperationLineResponse(l.accountNumber(), l.reference(), l.date(), l.description(),
                l.counterparty(), l.beneficiaryTin(), l.amount(), l.currency(), l.direction().name());
    }
}
