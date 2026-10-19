package az.corbank.mscorbank.adapter.out.pasha.dto.gpp;


import az.corbank.mscorbank.adapter.out.pasha.enums.FeeCalculationMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalculateCommissionRequestDto {

    private BigDecimal amount;

    private FeeCalculationMethod feeCalculationMethod;

    private InvoiceRequestDto invoiceRequest;
}