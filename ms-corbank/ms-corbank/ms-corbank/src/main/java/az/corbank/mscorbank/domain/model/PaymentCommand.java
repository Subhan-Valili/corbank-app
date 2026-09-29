package az.corbank.mscorbank.domain.model;

import java.math.BigDecimal;

public record PaymentCommand(
        String fromAccountId,
        String beneficiaryName,
        String beneficiaryIban,
        BigDecimal amount,
        String currency,
        String description,
        BigDecimal creditAmount,     // own-account / FX transfers: converted amount credited to the target
        String creditCurrency
) {
}
