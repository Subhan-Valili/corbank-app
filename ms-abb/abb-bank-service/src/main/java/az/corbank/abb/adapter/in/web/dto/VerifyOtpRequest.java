package az.corbank.abb.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;

public record VerifyOtpRequest(@NotBlank String batchNumber, @NotBlank String otpCode) {
}
