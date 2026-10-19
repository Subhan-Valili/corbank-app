package az.corbank.mscorbank.adapter.in.web.dto;

import lombok.Builder;

import java.math.BigDecimal;

/**
 * corbank-frontend-in gözlədiyi hesab forması (bax: js/hesablar.js, js/dashboard.js, js/account.js).
 */
@Builder
public record WebAccountDto(
        String id,
        String name,
        BigDecimal balance,
        String currency,
        String iban,
        String status,
        String bank      // "PASHA" | "ABB" — hansı bankın hesabı olduğunu frontend-ə bildirir
) {
}
