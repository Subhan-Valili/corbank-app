package az.ingress.mspashabank.dto.account;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Builder
public record DetailedStatementRequestDto(
        BigDecimal fromAmount,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
        LocalDateTime fromDate,
        PagingDto operationPaging,
        List<SortDto> operationSort,
        BigDecimal toAmount,
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
        LocalDateTime toDate
) {

    @Builder
    public record PagingDto(
            Integer page,
            Integer size
    ) {
    }

    /**
     * @param direction "ASC" və ya "DESC"
     * @param field     "DATE", "AMOUNT" və s.
     */
    @Builder
    public record SortDto(
            String direction,
            String field
    ) {
    }
}