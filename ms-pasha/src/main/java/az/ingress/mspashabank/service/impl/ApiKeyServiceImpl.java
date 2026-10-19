package az.ingress.mspashabank.service.impl;

import az.ingress.mspashabank.entity.ApiKeyEntity;
import az.ingress.mspashabank.entity.CustomerEntity;
import az.ingress.mspashabank.repository.ApiKeyRepository;
import az.ingress.mspashabank.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
@Slf4j
public class ApiKeyServiceImpl {

    private static final String KEY_PREFIX = "pasha_";
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final ApiKeyRepository apiKeyRepository;
    private final CustomerRepository customerRepository;

    /**
     * Müştəri üçün yeni key yaradır. Açıq key yalnız burada, bir dəfə qaytarılır,
     * DB-də yalnız SHA-256 hash saxlanılır.
     */
    @Transactional
    public String generate(String customerNo) {
        log.info("ActionLog.generate.start - customerNo: {}", customerNo);

        CustomerEntity customer = customerRepository.findByCustomerNo(customerNo)
                .orElseThrow(() -> {
                    log.error("ActionLog.generate.error - customer not found: {}", customerNo);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found");
                });

        byte[] random = new byte[32];
        SECURE_RANDOM.nextBytes(random);
        String plainKey = KEY_PREFIX + Base64.getUrlEncoder().withoutPadding().encodeToString(random);

        apiKeyRepository.save(ApiKeyEntity.builder()
                .keyHash(sha256(plainKey))
                .keyPrefix(plainKey.substring(0, 10))
                .customer(customer)
                .build());

        log.info("ActionLog.generate.end - customerNo: {}", customerNo);
        return plainKey;
    }

    /**
     * Key-i yoxlayır və sahibi olan müştərini qaytarır.
     * Xəta halında 401 atır (ms-corbank-da FeignException.Unauthorized kimi düşəcək).
     */
    @Transactional(readOnly = true)
    public CustomerEntity authenticate(String plainKey) {
        if (plainKey == null || plainKey.isBlank()) {
            log.warn("ActionLog.authenticate.error - api key is missing");
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "API key is missing");
        }

        ApiKeyEntity key = apiKeyRepository.findByKeyHashAndActiveTrue(sha256(plainKey))
                .orElseThrow(() -> {
                    log.warn("ActionLog.authenticate.error - invalid api key");
                    return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid API key");
                });

        if (key.getExpiresAt() != null && key.getExpiresAt().isBefore(LocalDateTime.now())) {
            log.warn("ActionLog.authenticate.error - api key expired, prefix: {}", key.getKeyPrefix());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "API key expired");
        }

        return key.getCustomer();
    }

    /** Key-i ləğv edir (active = false). */
    @Transactional
    public void revoke(String plainKey) {
        ApiKeyEntity key = apiKeyRepository.findByKeyHashAndActiveTrue(sha256(plainKey))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "API key not found"));
        key.setActive(false);
        log.info("ActionLog.revoke - prefix: {}", key.getKeyPrefix());
    }

    private static String sha256(String value) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}