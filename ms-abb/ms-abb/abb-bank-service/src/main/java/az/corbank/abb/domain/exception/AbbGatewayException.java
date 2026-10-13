package az.corbank.abb.domain.exception;

/**
 * Thrown by any AbbBankGateway implementation when it cannot fulfil a call — ABB rejected
 * the request, timed out, or was unreachable. Deliberately doesn't carry HTTP-specific
 * details (status codes, response bodies): those are an adapter concern. Application
 * services only need to know "the gateway failed" to decide whether to fall back to a
 * cached snapshot or propagate the failure.
 */
public class AbbGatewayException extends RuntimeException {
    public AbbGatewayException(String message) {
        super(message);
    }

    public AbbGatewayException(String message, Throwable cause) {
        super(message, cause);
    }
}
