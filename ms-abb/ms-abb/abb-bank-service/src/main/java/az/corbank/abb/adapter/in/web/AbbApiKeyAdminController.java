package az.corbank.abb.adapter.in.web;

import az.corbank.abb.application.service.AbbApiKeyService;
import az.corbank.abb.domain.model.CustomerIdentity;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

/** Operator-only provisioning. No browser access; disabled until a server-side admin token is configured. */
@RestController
@RequestMapping("/internal/abb/api-keys")
class AbbApiKeyAdminController {
    private final AbbApiKeyService keys;
    private final String adminToken;

    AbbApiKeyAdminController(AbbApiKeyService keys,
                             @Value("${abb.api.key-admin-token:}") String adminToken) {
        this.keys = keys;
        this.adminToken = adminToken;
    }

    @PostMapping
    Map<String, String> create(@RequestHeader(value = "X-Admin-Token", required = false) String suppliedToken,
                               @RequestBody CreateKeyRequest body) {
        requireAdmin(suppliedToken);
        String key = keys.generate(new CustomerIdentity(body.pin(), body.mobileNumber()), body.expiresAt());
        return Map.of("apiKey", key);
    }

    @DeleteMapping
    void revoke(@RequestHeader(value = "X-Admin-Token", required = false) String suppliedToken,
                @RequestBody RevokeKeyRequest body) {
        requireAdmin(suppliedToken);
        keys.revoke(body.apiKey());
    }

    private void requireAdmin(String suppliedToken) {
        if (adminToken.isBlank() || suppliedToken == null || !MessageDigest.isEqual(
                adminToken.getBytes(StandardCharsets.UTF_8), suppliedToken.getBytes(StandardCharsets.UTF_8))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Administrator credentials required");
        }
    }

    public record CreateKeyRequest(String pin, String mobileNumber, LocalDateTime expiresAt) {}
    public record RevokeKeyRequest(String apiKey) {}
}
