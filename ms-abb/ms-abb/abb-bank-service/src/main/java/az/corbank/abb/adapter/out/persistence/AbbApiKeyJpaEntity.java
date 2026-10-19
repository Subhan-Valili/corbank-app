package az.corbank.abb.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/** A raw API key is never persisted: only SHA-256 and a short non-sensitive prefix. */
@Entity
@Table(name = "abb_api_keys", indexes = @Index(name = "idx_abb_api_key_active", columnList = "active"))
public class AbbApiKeyJpaEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "key_hash", nullable = false, unique = true, length = 64)
    private String keyHash;

    @Column(name = "key_prefix", length = 12)
    private String keyPrefix;

    @Column(name = "customer_pin", length = 32)
    private String customerPin;

    @Column(name = "customer_mobile", length = 32)
    private String customerMobile;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    protected AbbApiKeyJpaEntity() {}

    public AbbApiKeyJpaEntity(String keyHash, String keyPrefix, String pin, String mobile, LocalDateTime expiresAt) {
        this.keyHash = keyHash;
        this.keyPrefix = keyPrefix;
        this.customerPin = pin;
        this.customerMobile = mobile;
        this.expiresAt = expiresAt;
    }

    public String getKeyHash() { return keyHash; }
    public String getKeyPrefix() { return keyPrefix; }
    public String getCustomerPin() { return customerPin; }
    public String getCustomerMobile() { return customerMobile; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public LocalDateTime getExpiresAt() { return expiresAt; }
}
