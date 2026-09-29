package az.corbank.abb.adapter.in.web.dto;

import az.corbank.abb.domain.model.BatchType;
import az.corbank.abb.domain.model.PaymentSubmission;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PaymentSubmitRequest(
        @NotNull BatchType type,
        @NotBlank String base64aDoc,
        String externalReference
) {
    public PaymentSubmission toDomain() {
        return new PaymentSubmission(type, base64aDoc, externalReference);
    }
}
