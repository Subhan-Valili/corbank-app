package az.corbank.mscorbank.domain.model;

import java.math.BigDecimal;

public record BankBalance(String name, BigDecimal balance, String currency) {
}
