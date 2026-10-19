package az.corbank.mscorbank.adapter.out.abb.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** ms-abb-bank-in PaymentSubmitResponse-in güzgüsü. status: "COMPLETED" | "REJECTED". */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AbbPaymentResponseDto(String status, String message, String reference) {
}
