package az.corbank.abb.domain.model;

import java.math.BigDecimal;
import java.time.Instant;

/** spec §4.9. */
public record AccountBalance(
        String accountNumber,
        String currency,
        BigDecimal availableBalance,
        Instant fetchedAt,
        DataOrigin origin
) {
}
