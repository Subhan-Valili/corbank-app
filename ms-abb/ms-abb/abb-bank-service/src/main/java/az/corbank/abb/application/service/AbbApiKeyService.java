package az.corbank.abb.application.service;

import az.corbank.abb.adapter.out.persistence.AbbApiKeyJpaEntity;
import az.corbank.abb.adapter.out.persistence.AbbApiKeyJpaRepository;
import az.corbank.abb.application.port.out.AbbBankGateway;
import az.corbank.abb.domain.model.CustomerIdentity;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

/** Same lifecycle as ms-pasha: 32 random bytes, SHA-256 at rest, active/expiry, customer scope. */
@Service
public class AbbApiKeyService {
    private static final String PREFIX = "abb_";
    private static final SecureRandom RANDOM = new SecureRandom();

    private final AbbApiKeyJpaRepository repository;
    private final AbbBankGateway bank;

    public AbbApiKeyService(AbbApiKeyJpaRepository repository, AbbBankGateway bank) {
        this.repository = repository;
        this.bank = bank;
    }

    @Transactional
    public String generate(CustomerIdentity customer, LocalDateTime expiresAt) {
        // A key must never be issued for an identity that does not have ABB accounts.
        if (bank.listAccounts(customer).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "ABB customer has no accounts");
        }
        byte[] random = new byte[32];
        RANDOM.nextBytes(random);
        String key = PREFIX + Base64.getUrlEncoder().withoutPadding().encodeToString(random);
        repository.save(new AbbApiKeyJpaEntity(sha256(key), key.substring(0, 10),
                customer.pin(), customer.mobileNumber(), expiresAt));
        return key; // returned exactly once; never logged
    }

    @Transactional(readOnly = true)
    public CustomerIdentity authenticate(String plainKey) {
        if (plainKey == null || plainKey.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "API key is missing");
        }
        AbbApiKeyJpaEntity key = repository.findByKeyHashAndActiveTrue(sha256(plainKey))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid API key"));
        if (key.getExpiresAt() != null && !key.getExpiresAt().isAfter(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "API key expired");
        }
        return new CustomerIdentity(key.getCustomerPin(), key.getCustomerMobile());
    }

    @Transactional
    public void revoke(String plainKey) {
        if (plainKey == null || plainKey.isBlank()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "API key not found");
        }
        AbbApiKeyJpaEntity key = repository.findByKeyHashAndActiveTrue(sha256(plainKey))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "API key not found"));
        key.setActive(false);
    }

    public static String sha256(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }
}
