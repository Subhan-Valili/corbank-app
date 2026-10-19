package az.corbank.mscorbank.domain.model;

import java.math.BigDecimal;

public record Account(
        String id,
        String name,
        BigDecimal balance,
        String currency,
        String iban,
        AccountStatus status,
        Bank bank
) {
}
