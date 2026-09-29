package az.ingress.mspashabank.dto.account;

import java.util.List;

public record AccountOperationsSearchResponseDto(
        List<AccountOperationDto> data,
        PaginationDto pagination
) {

    public record PaginationDto(
            Integer count,
            Boolean hasNextPage,
            Integer offset,
            Integer total
    ) {
    }
}