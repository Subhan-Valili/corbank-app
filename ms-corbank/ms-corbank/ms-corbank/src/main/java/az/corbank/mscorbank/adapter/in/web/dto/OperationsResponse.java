package az.corbank.mscorbank.adapter.in.web.dto;

import java.util.List;

public record OperationsResponse(
        List<WebOperationDto> operations,
        OperationsSummaryDto summary
) {
}
