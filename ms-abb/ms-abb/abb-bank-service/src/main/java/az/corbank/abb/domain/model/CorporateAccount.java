package az.corbank.abb.domain.model;

import java.math.BigDecimal;

/** One row of spec §4.21 (GET all accounts by CIF) — this is the "list my accounts" source. */
public record CorporateAccount(
        String name,
        String currency,
        String accountNumber,
        String iban,
        BigDecimal availableBalance
) {
}
