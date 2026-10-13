package az.corbank.abb.domain.exception;

/**
 * The account doesn't exist OR doesn't belong to the calling customer. Both cases look the
 * same on purpose, so a caller can't probe which account numbers exist.
 */
public class AccountNotFoundException extends RuntimeException {
    public AccountNotFoundException(String message) {
        super(message);
    }
}
