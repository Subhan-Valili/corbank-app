package az.corbank.mscorbank.application.exception;

/** Verilən PIN ilə heç bir inteqrasiya olunmuş bankda müştəri tapılmadı. */
public class PinNotFoundException extends RuntimeException {
    public PinNotFoundException(String message) {
        super(message);
    }
}
