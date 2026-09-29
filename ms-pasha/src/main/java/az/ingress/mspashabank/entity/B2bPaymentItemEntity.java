package az.ingress.mspashabank.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Entity
@Table(name = "b2b_payment_items")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class B2bPaymentItemEntity extends BaseEntity {

    @Column(name = "type")
    String type;

    @Column(name = "amount", precision = 15, scale = 2)
    BigDecimal amount;

    @Column(name = "commission_account")
    String commissionAccount;

    @Column(name = "description")
    String description;

    @Column(name = "file_required")
    Boolean fileRequired;

    @Column(name = "urgent")
    Boolean urgent;

    @Column(name = "reference_number")
    String referenceNumber;

    // Payer Info
    @Column(name = "payer_account_number")
    String payerAccountNumber;

    // Payee Info
    @Column(name = "payee_account_number")
    String payeeAccountNumber;

    @Column(name = "payee_name")
    String payeeName;

    @Column(name = "payee_tin")
    String payeeTin;

    @Column(name = "payee_type")
    String payeeType;

    @Column(name = "payee_email")
    String payeeEmail;

    @Column(name = "payee_address")
    String payeeAddress;

    @Column(name = "payee_additional_info")
    String payeeAdditionalInfo;

    @Column(name = "payee_bank_code")
    String payeeBankCode;

    @Column(name = "payee_bank_name")
    String payeeBankName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bulk_payment_id")
    B2bBulkPaymentEntity bulkPayment;
}