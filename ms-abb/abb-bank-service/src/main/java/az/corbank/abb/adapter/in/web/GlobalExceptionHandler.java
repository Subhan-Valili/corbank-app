package az.corbank.abb.adapter.in.web;

import az.corbank.abb.domain.exception.AbbGatewayException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;

@Slf4j
@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(AbbGatewayException.class)
    ResponseEntity<Map<String, Object>> handleGateway(AbbGatewayException ex) {
        return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                .body(errorBody("ABB_UPSTREAM_ERROR", "ABB Bank API rejected or failed the request."));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        String detail = ex.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .reduce((a, b) -> a + "; " + b)
                .orElse("Invalid request");
        return ResponseEntity.badRequest().body(errorBody("VALIDATION_ERROR", detail));
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
        log.error("Unhandled exception in abb-bank-service", ex);
        return ResponseEntity.internalServerError().body(errorBody("INTERNAL_ERROR", "Something went wrong."));
    }

    private Map<String, Object> errorBody(String code, String message) {
        return Map.of("timestamp", Instant.now().toString(), "code", code, "message", message);
    }
}
