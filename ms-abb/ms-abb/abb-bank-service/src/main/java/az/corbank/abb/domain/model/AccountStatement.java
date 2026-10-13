package az.corbank.abb.domain.model;

import java.math.BigDecimal;
import java.util.List;

public record AccountStatement(
        String accountNumber,
        String currency,
        BigDecimal openingBalance,
        BigDecimal closingBalance,
        BigDecimal periodIncome,
        BigDecimal periodExpense,
        int page,
        int pageCount,
        long itemsCount,
        List<StatementLine> lines
) {
    /** Computes period totals from the line list — a small piece of real domain logic. */
    public static AccountStatement of(String accountNumber, String currency, BigDecimal openingBalance,
                                        BigDecimal closingBalance, int page, int pageCount, long itemsCount,
                                        List<StatementLine> lines) {
        BigDecimal income = sum(lines, Direction.IN);
        BigDecimal expense = sum(lines, Direction.OUT);
        return new AccountStatement(accountNumber, currency, openingBalance, closingBalance,
                income, expense, page, pageCount, itemsCount, lines);
    }

    private static BigDecimal sum(List<StatementLine> lines, Direction direction) {
        return lines.stream()
                .filter(l -> l.direction() == direction)
                .map(StatementLine::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
