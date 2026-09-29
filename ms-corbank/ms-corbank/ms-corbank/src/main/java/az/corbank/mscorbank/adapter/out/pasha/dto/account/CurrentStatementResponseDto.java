package az.corbank.mscorbank.adapter.out.pasha.dto.account;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CurrentStatementResponseDto {

    BigDecimal availableClosingBalance;
    BigDecimal availableOpeningBalance;
    BigDecimal closingBalance;
    BigDecimal closingBalanceAzn;
    String message;
    BigDecimal openingBalance;
    List<StatementOperationDto> operations;
    PaginationMetaDataDto paginationMetaData;
}