package az.corbank.mscorbank.adapter.out.abb.dto;

import lombok.Builder;

import java.math.BigDecimal;

/** ms-abb-bank-in PaymentSubmitRequest-in güzgüsü (bax abb-bank-service/README.md). */
@Builder
public record AbbPaymentRequestDto(
        String fromAccountNumber,
        String beneficiaryAccountNumber,
        String beneficiaryName,
        BigDecimal amount,
        String currency,
        String description,
        BigDecimal creditAmount,
        String creditCurrency
) {
}
