package az.corbank.abb.adapter.in.web.dto;

import az.corbank.abb.domain.model.PaymentInstruction;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record PaymentSubmitRequest(
        @NotBlank String fromAccountNumber,
        @NotBlank String beneficiaryAccountNumber,
        String beneficiaryName,
        @NotNull @Positive BigDecimal amount,
        @NotBlank String currency,
        String description,
        @Positive BigDecimal creditAmount,
        String creditCurrency
) {
    public PaymentInstruction toDomain() {
        return new PaymentInstruction(fromAccountNumber, beneficiaryAccountNumber, beneficiaryName,
                amount, currency, description, creditAmount, creditCurrency);
    }
}
