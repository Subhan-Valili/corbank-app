package az.corbank.mscorbank.adapter.out.pasha.dto.account;



import az.corbank.mscorbank.adapter.out.pasha.enums.OperationSource;
import az.corbank.mscorbank.adapter.out.pasha.enums.OperationType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AccountOperationDto(
        String id,
        AmountDto amount,
        CounterpartyDto counterparty,
        LocalDateTime date,
        String description,
        OperationSource source,
        OperationType type
) {

    public record AmountDto(
            String currencyCode,
            BigDecimal value
    ) {
    }

    public record CounterpartyDto(
            String id,
            String name
    ) {
    }
}
