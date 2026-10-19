package az.ingress.mspashabank.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@Table(name = "api_keys", indexes = {
        @Index(name = "idx_api_key_hash", columnList = "key_hash")
})
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Getter
@Setter
@ToString(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ApiKeyEntity extends BaseEntity {

    @Column(name = "key_hash", unique = true, nullable = false, length = 64)
    String keyHash; // SHA-256(hex)

    @Column(name = "key_prefix", length = 12)
    String keyPrefix; // debug üçün: "pasha_a1b2"

    @Builder.Default
    @Column(name = "active", nullable = false)
    Boolean active = true;

    @Column(name = "expires_at")
    LocalDateTime expiresAt;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    CustomerEntity customer;
}
