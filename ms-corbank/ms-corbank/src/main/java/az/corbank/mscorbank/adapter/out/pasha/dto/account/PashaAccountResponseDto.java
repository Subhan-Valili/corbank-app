package az.corbank.mscorbank.adapter.out.pasha.dto.account;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
public record PashaAccountResponseDto(
        String accountId,
        String customerNo,
        String accountCategory,
        String accountNo,
        LocalDate accountOpenDate,
        String accountStatus,
        String accountType,
        BigDecimal availableBalance,
        BigDecimal currentBalance,
        BigDecimal blockedAmount,
        String currency,
        String iban,
        String bankCode,
        String branchCode,
        String branchName,
        String tin,
        Boolean creditIsAllowed,
        Boolean debitIsAllowed,
        Boolean hasCard,
        Boolean hasCredit,
        Boolean hasPos,
        BigDecimal todayIncome,
        BigDecimal todayOpeningBalance,
        BigDecimal todayOutcome
) {}
