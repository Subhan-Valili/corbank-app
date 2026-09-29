package az.corbank.abb.application.port.in;

public interface VerifyOtpUseCase {
    /** Returns ABB's raw status text (e.g. "Successfully authorized") — spec §4.5. */
    String verifyOtp(String batchNumber, String otpCode);
}
