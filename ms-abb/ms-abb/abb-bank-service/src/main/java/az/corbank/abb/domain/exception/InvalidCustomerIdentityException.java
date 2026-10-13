package az.corbank.abb.domain.exception;

/** Neither a PIN nor a mobile number was supplied, so we can't tell whose data to return. */
public class InvalidCustomerIdentityException extends RuntimeException {
    public InvalidCustomerIdentityException(String message) {
        super(message);
    }
}
