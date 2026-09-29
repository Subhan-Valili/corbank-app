package az.ingress.mspashabank.dto.bulk;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Builder
public record FxRatesResponse(
        List<FxRateDto> fxRates,
        Boolean isEditableRate
) {

    @Builder
    public record FxRateDto(
            String currency,
            PrefRateDto prefRate,
            BigDecimal standardBuyRate,
            BigDecimal standardSellRate,
            String targetCurrency,
            @JsonFormat(shape = JsonFormat.Shape.STRING)
            Instant validUntil
    ) {
    }

    @Builder
    public record PrefRateDto(
            BigDecimal buyCommission,
            BigDecimal buyRate,
            BigDecimal sellCommission,
            BigDecimal sellRate
    ) {
    }
}