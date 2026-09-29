package az.ingress.mspashabank.dto.gpp;

import az.ingress.mspashabank.enums.FeeCalculationMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceInfoDto {
    private String id;
    private String invoiceType;
    private FeeCalculationMethod feeCalculationMethod;
    private BigDecimal currentDebt;
    private BigDecimal commissionAmount;
    private BigDecimal minAllowedAmount;
    private BigDecimal maxAllowedAmount;
    private Boolean partialPaymentAllowed;
    private Integer serviceCode;
    private String serviceDescription;
    private Integer paymentReceiverCode;
    private String paymentReceiverDescription;
    private List<PaymentReceiverDto> paymentReceivers;
}