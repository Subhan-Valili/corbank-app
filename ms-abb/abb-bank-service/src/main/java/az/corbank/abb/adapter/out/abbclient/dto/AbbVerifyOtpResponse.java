package az.corbank.abb.adapter.out.abbclient.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Response of POST /payments/verify-otp — spec §4.5, e.g. {"status": "Successfully authorized"}. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AbbVerifyOtpResponse(String status) {
}
