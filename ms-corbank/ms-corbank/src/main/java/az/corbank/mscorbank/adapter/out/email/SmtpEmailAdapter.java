package az.corbank.mscorbank.adapter.out.email;

import az.corbank.mscorbank.application.port.out.EmailSenderPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class SmtpEmailAdapter implements EmailSenderPort {
    private final JavaMailSender mailSender;

    @Override
    @Async
    public void sendWelcomeEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            mailSender.send(message);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
        }


    }

    @Override
    @Async
    public void sendOtpEmail(String to, String otp) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject("CorBank: OTP kodunuz");
            message.setText("Təsdiq kodunuz: " + otp + ". Kod 5 dəqiqə etibarlıdır.");
            mailSender.send(message);
        } catch (Exception ex) {
            log.error("OTP email göndərilmədi: {}", to, ex);
        }
    }
}
