package az.ingress.mspashabank.dto.gpp;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InvoiceDto {
    String id;
    String invoiceCode;
    String invoiceType;
    String stateCode;
    String receiptNumber;
    String feeCalculationMethod;
    BigDecimal amountDue;
    BigDecimal commissionAmount;
    BigDecimal currentDebt;
    BigDecimal maxAllowedAmount;
    BigDecimal minAllowedAmount;
    Boolean partialPaymentAllowed;
    Boolean manualPaymentReceiverSelectionRequired;
    Boolean manualSpBranchSelectionRequired;
    Integer serviceCode;
    String serviceDescription;
    Integer spBranchCode;
    String spBranchDescription;
    Integer paymentReceiverCode;
    String paymentReceiverDescription;
    List<PaymentReceiverDto> paymentReceivers;
    List<ChildInvoiceDto> childInvoiceList;
}