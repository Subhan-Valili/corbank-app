package az.corbank.abb.adapter.out.abbclient.dto;

/** Request body of POST /payments/verify-otp — spec §4.5. */
public record AbbVerifyOtpRequest(String batchNumber, String otpCode) {
}
