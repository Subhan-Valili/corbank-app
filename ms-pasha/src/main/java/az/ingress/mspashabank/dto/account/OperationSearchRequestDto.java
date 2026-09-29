package az.ingress.mspashabank.dto.account;

import java.util.List;

public record OperationSearchRequestDto(
        FilterDto filter,
        PaginationDto pagination,
        List<SortRequestDto> sort
) {

    /**
     * @param activation NOT: AccountOperationEntity-də qarşılığı yoxdur, yalnız loglanır.
     */
    public record FilterDto(
            String activation,
            AmountRangeDto amountValue,
            DateRangeDto date,
            String searchValue
    ) {
    }

    public record AmountRangeDto(
            String from,
            String to
    ) {
    }

    public record DateRangeDto(
            String from,
            String to
    ) {
    }

    /**
     * Request tərəfində yalnız offset və count istifadə olunur.
     */
    public record PaginationDto(
            Integer count,
            Integer offset
    ) {
    }

    /**
     * @param order   "ASC" | "DESC"
     * @param orderBy "date", "amount", "type", "source", "description", "id"
     */
    public record SortRequestDto(
            String order,
            String orderBy
    ) {
    }
}