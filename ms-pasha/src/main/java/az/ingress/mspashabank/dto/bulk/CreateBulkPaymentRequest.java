package az.ingress.mspashabank.dto.bulk;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

@Builder
public record CreateBulkPaymentRequest(
        String bulkDescription,
        List<PaymentRequest> payments
) {

    @Builder
    public record PaymentRequest(
            String type,
            BigDecimal amount,
            String commissionAccount,
            String description,
            Boolean fileRequired,
            PayeeDto payee,
            PayerDto payer,
            String referenceNumber,
            Boolean urgent
    ) {
    }
}