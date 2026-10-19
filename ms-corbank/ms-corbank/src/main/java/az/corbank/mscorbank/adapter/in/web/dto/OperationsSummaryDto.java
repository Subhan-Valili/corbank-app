package az.corbank.mscorbank.adapter.in.web.dto;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record OperationsSummaryDto(
        BigDecimal income,
        BigDecimal expense,
        BigDecimal net
) {
}
