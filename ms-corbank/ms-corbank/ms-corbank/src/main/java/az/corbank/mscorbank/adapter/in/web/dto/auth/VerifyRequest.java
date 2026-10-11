package az.corbank.mscorbank.adapter.in.web.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record VerifyRequest(
        @NotBlank
        String signUpSessionId,

        @NotBlank
        @Pattern(regexp = "\\d{6}", message = "OTP 6 rəqəmdən ibarət olmalıdır")
        String otp
) {}