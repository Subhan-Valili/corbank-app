package az.corbank.mscorbank.adapter.out.abb.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

/** ms-abb-bank-in AccountStatementResponse-in güzgüsü — ABB spec §4.10 (canlı çıxarış). */
@Builder
public record AbbStatementResponseDto(
        String accountNumber,
        String currency,
        BigDecimal openingBalance,
        BigDecimal closingBalance,
        BigDecimal periodIncome,
        BigDecimal periodExpense,
        int page,
        int pageCount,
        long itemsCount,
        List<AbbOperationLineDto> transactions
) {
}
