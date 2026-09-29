package az.ingress.mspashabank.dto.gpp;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChildInvoiceDto {
    String id;
    String invoiceCode;
    String receiptNumber;
    String feeCalculationMethod;
    BigDecimal amount;
    BigDecimal commissionAmount;
    Integer serviceCode;
    String serviceDescription;
    Integer paymentReceiverCode;
    String paymentReceiverDescription;
}