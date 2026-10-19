package az.corbank.mscorbank.adapter.in.web;

import az.corbank.mscorbank.application.exception.AccountNotFoundException;
import az.corbank.mscorbank.application.exception.BankConnectionException;
import az.corbank.mscorbank.application.exception.InvalidApiKeyException;
import az.corbank.mscorbank.application.exception.RegistrationException;
import az.corbank.mscorbank.application.exception.ServiceUnavailableException;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    /** Redis (OTP/sessiya saxlancı) işləmirsə 500 + 401 yox, aydın 503 qaytar. */
    @ExceptionHandler(RedisConnectionFailureException.class)
    public ResponseEntity<Map<String, String>> redisDown(RedisConnectionFailureException e) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("message", "Sistem müvəqqəti əlçatmazdır, bir az sonra yenidən cəhd edin"));
    }

    @ExceptionHandler(RegistrationException.class)
    public ResponseEntity<Map<String, String>> registration(RegistrationException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, String>> badCredentials(BadCredentialsException e) {
        String msg = "Bad credentials".equals(e.getMessage()) ? "Email və ya şifrə yanlışdır" : e.getMessage();
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", msg));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> invalid(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(fe -> fe.getDefaultMessage() != null ? fe.getDefaultMessage() : "Məlumatlar düzgün deyil")
                .orElse("Məlumatlar düzgün deyil");
        return ResponseEntity.badRequest().body(Map.of("message", msg));
    }

    /** Bank API Key-i qəbul etmədi -> 422. */
    @ExceptionHandler(InvalidApiKeyException.class)
    public ResponseEntity<Map<String, String>> invalidApiKey(InvalidApiKeyException e) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(Map.of("message", e.getMessage()));
    }

    @ExceptionHandler(BankConnectionException.class)
    public ResponseEntity<Map<String, String>> bankConnection(BankConnectionException e) {
        return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
    }

    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<Map<String, String>> accountNotFound(AccountNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", e.getMessage()));
    }

    @ExceptionHandler(ServiceUnavailableException.class)
    public ResponseEntity<Map<String, String>> unavailable(ServiceUnavailableException e) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of("message", e.getMessage()));
    }
}
