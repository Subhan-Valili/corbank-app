package az.ingress.mspashabank.dto.bulk;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

@Builder
public record CreateSalaryBulkPaymentRequest(
        String bulkDescription,
        List<SalaryPaymentItemDto> payments
) {

    @Builder
    public record SalaryPaymentItemDto(
            BigDecimal amount,
            String commissionAccount,
            String description,
            PayeeDto payee,
            PayerDto payer,
            String referenceNumber,
            String salaryType,
            String type
    ) {
    }
}