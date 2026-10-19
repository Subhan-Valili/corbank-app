package az.corbank.mscorbank.adapter.in.web.dto;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record BankBalanceDto(
        String name,
        BigDecimal balance,
        String currency
) {
}
