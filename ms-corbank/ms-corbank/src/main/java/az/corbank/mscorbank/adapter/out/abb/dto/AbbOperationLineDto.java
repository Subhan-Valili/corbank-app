package az.corbank.mscorbank.adapter.out.abb.dto;

import lombok.Builder;

import java.math.BigDecimal;

/** ms-abb-bank-in OperationLineResponse-in güzgüsü. amount həmişə müsbətdir. */
@Builder
public record AbbOperationLineDto(
        String accountNumber,
        String reference,
        String date,          // dd.MM.yyyy, ABB-nin verdiyi formatda
        String description,
        String counterparty,
        String beneficiaryTin,
        BigDecimal amount,
        String currency,
        String direction      // "IN" | "OUT"
) {
}
