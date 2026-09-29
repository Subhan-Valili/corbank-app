package az.ingress.mspashabank.entity;

import az.ingress.mspashabank.enums.OperationSource;
import az.ingress.mspashabank.enums.OperationStatus;
import az.ingress.mspashabank.enums.OperationType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "account_operations", indexes = {
        @Index(name = "idx_operation_external_id", columnList = "external_id"),
        @Index(name = "idx_operation_account_id", columnList = "pasha_account_id"),
        @Index(name = "idx_operation_date", columnList = "operation_date")
})
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Getter
@Setter
@ToString(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AccountOperationEntity extends BaseEntity {

    @Column(name = "external_id", unique = true, nullable = false)
    String externalId;

    @Column(name = "amount_value", precision = 15, scale = 2, nullable = false)
    BigDecimal amountValue;

    @Column(name = "amount_currency_code", length = 3, nullable = false)
    String amountCurrencyCode;

    @Column(name = "counterparty_id")
    String counterpartyId;

    @Column(name = "counterparty_name")
    String counterpartyName;

    @Column(name = "operation_date", nullable = false)
    LocalDateTime operationDate;

    @Column(name = "description")
    String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false)
    OperationSource source;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    OperationType type;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    OperationStatus status = OperationStatus.COMPLETED;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pasha_account_id", nullable = false)
    PashaAccountEntity pashaAccount;

}