package az.corbank.mscorbank.adapter.in.web.dto;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record FxRateViewDto(
        String currency,
        BigDecimal buy,
        BigDecimal sell
) {
}
