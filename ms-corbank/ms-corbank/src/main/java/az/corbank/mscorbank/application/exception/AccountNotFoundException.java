package az.corbank.mscorbank.application.exception;

/** Hesab yoxdur VƏ YA cari istifadəçiyə aid deyil (məlumat sızmasın deyə ikisi eyni cavab verir). */
public class AccountNotFoundException extends RuntimeException {
    public AccountNotFoundException(String accountId) {
        super("Hesab tapılmadı: " + accountId);
    }
}
