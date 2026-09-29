package az.ingress.mspashabank.dto.bulk;

import lombok.Builder;

import java.util.List;

@Builder
public record ApproveBulkPaymentRequest(
        Long bulkId,
        List<String> referenceNumbers
) {}