package az.corbank.abb.adapter.in.web.dto;

import az.corbank.abb.domain.model.AccountStatement;

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
        List<OperationLineResponse> transactions
) {
    public static AccountStatementResponse from(AccountStatement s) {
        return new AccountStatementResponse(
                s.accountNumber(), s.currency(), s.openingBalance(), s.closingBalance(),
                s.periodIncome(), s.periodExpense(), s.page(), s.pageCount(), s.itemsCount(),
                s.lines().stream().map(OperationLineResponse::from).toList());
    }
}
