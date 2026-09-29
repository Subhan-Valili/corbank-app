package az.ingress.mspashabank.dto.account;

import az.ingress.mspashabank.enums.OperationSource;
import az.ingress.mspashabank.enums.OperationType;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record StatementOperationDto(
        String accountCurrency,
        BigDecimal afterOperationAvlBalance,
        BigDecimal afterOperationBalance,
        BigDecimal amountInAccountCurrency,
        BigDecimal amountInTransactionCurrency,
        BigDecimal amountInTransactionCurrencyAzn,
        String cardNo,
        BigDecimal closingAvlBalance,
        BigDecimal closingBalance,
        BigDecimal closingBalanceAzn,
        String counterParty,
        String counterPartyId,
        String counterPartyName,
        String counterPartyPin,
        String counterPartyTin,
        BigDecimal openingAvlBalance,
        BigDecimal openingBalance,

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
        LocalDateTime operationDate,
        OperationSource sourceSystem,
        String transactionCurrency,

        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
        LocalDateTime transactionDate,
        String transactionDescription,
        BigDecimal transactionFXRate,
        String transactionNo,
        OperationType transactionType
) {}