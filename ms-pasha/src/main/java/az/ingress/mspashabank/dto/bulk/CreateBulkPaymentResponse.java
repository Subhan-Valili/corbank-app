package az.ingress.mspashabank.dto.bulk;

import lombok.Builder;

@Builder
public record CreateBulkPaymentResponse(
        Long bulkId,
        String description,
        Integer recordCount
) {}