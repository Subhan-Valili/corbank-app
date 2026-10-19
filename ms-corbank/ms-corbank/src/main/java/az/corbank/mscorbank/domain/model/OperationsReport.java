package az.corbank.mscorbank.domain.model;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

public record OperationsReport(List<Operation> operations, OperationsSummary summary) {

    public static OperationsReport of(List<Operation> operations) {
        BigDecimal income = total(operations, OperationDirection.IN);
        BigDecimal expense = total(operations, OperationDirection.OUT);
        return new OperationsReport(operations, new OperationsSummary(income, expense, income.subtract(expense)));
    }

    private static BigDecimal total(List<Operation> operations, OperationDirection direction) {
        return operations.stream()
                .filter(op -> op.direction() == direction)
                .map(Operation::amount)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
