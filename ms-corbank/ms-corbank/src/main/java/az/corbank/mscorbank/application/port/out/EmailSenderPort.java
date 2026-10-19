package az.corbank.mscorbank.application.port.out;

public interface EmailSenderPort {
    void sendWelcomeEmail(String to, String subject, String text);
    void sendOtpEmail(String to, String otp);
}
