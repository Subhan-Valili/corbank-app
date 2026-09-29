package az.ingress.mspashabank.dto.account;

import az.ingress.mspashabank.enums.OperationSource;
import az.ingress.mspashabank.enums.OperationType;

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
