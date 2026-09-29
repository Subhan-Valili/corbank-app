package az.ingress.mspashabank.dto.account;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

@Builder
public record CurrentStatementResponseDto(
        BigDecimal availableClosingBalance,
        BigDecimal availableOpeningBalance,
        BigDecimal closingBalance,
        BigDecimal closingBalanceAzn,
        String message,
        BigDecimal openingBalance,
        List<StatementOperationDto> operations,
        PaginationMetaDataDto paginationMetaData
) {

    @Builder
    public record PaginationMetaDataDto(
            int currentPage,
            boolean hasNextPage,
            boolean hasPreviousPage,
            int totalPages
    ) {
    }
}