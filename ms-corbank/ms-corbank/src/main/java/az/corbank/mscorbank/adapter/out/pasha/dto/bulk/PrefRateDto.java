package az.corbank.mscorbank.adapter.out.pasha.dto.bulk;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrefRateDto {
    private BigDecimal buyCommission;
    private BigDecimal buyRate;
    private BigDecimal sellCommission;
    private BigDecimal sellRate;
}