package az.corbank.mscorbank.adapter.out.persistence.entity;

import az.corbank.mscorbank.domain.model.Bank;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

/** İstifadəçinin bir bank üçün Open API Key-i (AES-GCM ilə şifrələnmiş). (user, bank) unikaldır. */
@Entity
@Table(name = "bank_api_keys",
        uniqueConstraints = @UniqueConstraint(name = "uq_bank_api_keys_user_bank", columnNames = {"user_id", "bank"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BankApiKeyEntity extends BaseEntity {

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    UserEntity user;

    @Enumerated(EnumType.STRING)
    @Column(name = "bank", nullable = false, length = 30)
    Bank bank;

    @ToString.Exclude
    @Column(name = "api_key_encrypted", nullable = false, length = 1024)
    String apiKeyEncrypted;
}
