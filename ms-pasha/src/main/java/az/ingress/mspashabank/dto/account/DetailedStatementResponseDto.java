package az.ingress.mspashabank.dto.account;

import lombok.Builder;

import java.util.List;

@Builder
public record DetailedStatementResponseDto(
        List<StatementOperationDto> content,
        int pageNumber,
        int pageSize,
        long totalElements,
        int totalPages
) {}