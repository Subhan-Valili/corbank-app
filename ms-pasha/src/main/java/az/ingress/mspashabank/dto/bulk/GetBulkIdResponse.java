package az.ingress.mspashabank.dto.bulk;

import lombok.Builder;

@Builder
public record GetBulkIdResponse(
        Long bulkId,
        String referenceNumber
) {}