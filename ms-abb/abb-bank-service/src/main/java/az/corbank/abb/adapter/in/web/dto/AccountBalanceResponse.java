package az.corbank.abb.adapter.in.web.dto;

import az.corbank.abb.domain.model.AccountBalance;

import java.math.BigDecimal;
import java.time.Instant;

public record AccountBalanceResponse(
        String accountNumber,
        String currency,
        BigDecimal availableBalance,
        Instant fetchedAt,
        String source   // "LIVE" | "SNAPSHOT_FALLBACK"
) {
    public static AccountBalanceResponse from(AccountBalance b) {
        return new AccountBalanceResponse(b.accountNumber(), b.currency(), b.availableBalance(),
                b.fetchedAt(), b.origin().name());
    }
}
