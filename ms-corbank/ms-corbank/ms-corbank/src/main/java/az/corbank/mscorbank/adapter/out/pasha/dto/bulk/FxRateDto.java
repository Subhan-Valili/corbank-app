package az.corbank.mscorbank.adapter.out.pasha.dto.bulk;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FxRateDto {
    private String currency;
    private PrefRateDto prefRate;
    private BigDecimal standardBuyRate;
    private BigDecimal standardSellRate;
    private String targetCurrency;

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant validUntil;
}