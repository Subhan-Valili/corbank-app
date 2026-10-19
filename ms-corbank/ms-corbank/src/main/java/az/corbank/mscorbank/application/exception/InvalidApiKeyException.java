package az.corbank.mscorbank.application.exception;

/** Bank istifadəçinin daxil etdiyi Open API Key-i qəbul etmədi. */
public class InvalidApiKeyException extends RuntimeException {
    public InvalidApiKeyException(String message) {
        super(message);
    }
}
