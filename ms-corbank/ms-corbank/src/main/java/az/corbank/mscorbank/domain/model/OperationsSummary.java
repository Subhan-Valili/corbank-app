package az.corbank.mscorbank.domain.model;

import java.math.BigDecimal;

public record OperationsSummary(BigDecimal income, BigDecimal expense, BigDecimal net) {
}
