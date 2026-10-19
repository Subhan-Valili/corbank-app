package az.corbank.mscorbank.application.exception;

/** Qeydiyyat/OTP zamanı istifadəçiyə göstərilməli olan xəta (400). */
public class RegistrationException extends RuntimeException {
    public RegistrationException(String message) {
        super(message);
    }

    public RegistrationException(String message, Throwable cause) {
        super(message, cause);
    }
}
