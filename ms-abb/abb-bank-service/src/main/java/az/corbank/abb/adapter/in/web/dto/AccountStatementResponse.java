package az.corbank.abb.adapter.in.web.dto;

import az.corbank.abb.domain.model.AccountStatement;
import az.corbank.abb.domain.model.StatementLine;

import java.math.BigDecimal;
import java.util.List;

public record AccountStatementResponse(
        String accountNumber,
        String currency,
        BigDecimal openingBalance,
        BigDecimal closingBalance,
        BigDecimal periodIncome,
        BigDecimal periodExpense,
        int page,
        int pageCount,
        long itemsCount,
        List<Line> transactions
) {
    public record Line(String reference, String date, String description, String counterparty,
                        String beneficiaryTin, BigDecimal amount, String direction) {
        static Line from(StatementLine l) {
            return new Line(l.reference(), l.date(), l.description(), l.counterparty(),
                    l.beneficiaryTin(), l.amount(), l.direction().name());
        }
    }

    public static AccountStatementResponse from(AccountStatement s) {
        return new AccountStatementResponse(
                s.accountNumber(), s.currency(), s.openingBalance(), s.closingBalance(),
                s.periodIncome(), s.periodExpense(), s.page(), s.pageCount(), s.itemsCount(),
                s.lines().stream().map(Line::from).toList());
    }
}
