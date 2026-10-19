package az.corbank.mscorbank.application.exception;

/** Bank qoşma sorğusunda istifadəçiyə göstərilməli olan xəta (400). */
public class BankConnectionException extends RuntimeException {
    public BankConnectionException(String message) {
        super(message);
    }
}
