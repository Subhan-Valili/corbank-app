package az.ingress.mspashabank.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "pasha_accounts", indexes = {
        @Index(name = "idx_pasha_iban", columnList = "iban"),
        @Index(name = "idx_pasha_customer_no", columnList = "customer_no")
})
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@Getter
@Setter
@ToString(callSuper = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PashaAccountEntity extends BaseEntity {

    @Column(name = "account_id", unique = true, nullable = false)
    String accountId; // "ACC-1001"

    @Column(name = "customer_no", nullable = false)
    String customerNo; // "CUST-998877"

    @Column(name = "account_category")
    String accountCategory; // "corporate" və ya "individual"

    @Column(name = "account_no")
    String accountNo; // "1002345678"

    @Column(name = "account_open_date")
    LocalDate accountOpenDate;

    @Column(name = "account_status")
    String accountStatus; // "ACTIVE"

    @Column(name = "account_type")
    String accountType; // "CURRENT", "CARD", "OTHER"

    @Column(name = "available_balance", precision = 15, scale = 2)
    BigDecimal availableBalance; // 64320.00

    @Column(name = "current_balance", precision = 15, scale = 2)
    BigDecimal currentBalance;

    @Column(name = "blocked_amount", precision = 15, scale = 2)
    BigDecimal blockedAmount;

    @Column(name = "currency", length = 3, nullable = false)
    String currency; // "AZN", "USD", "EUR"

    @Column(name = "iban", unique = true, nullable = false)
    String iban; // "AZ21CORB00000000123456789012"

    @Column(name = "bank_code")
    String bankCode; // "PASHAAZ22"

    @Column(name = "branch_code")
    String branchCode;

    @Column(name = "branch_name")
    String branchName;

    @Column(name = "tin")
    String tin; // VÖEN

    @Column(name = "credit_is_allowed")
    Boolean creditIsAllowed;

    @Column(name = "debit_is_allowed")
    Boolean debitIsAllowed;

    @Column(name = "has_card")
    Boolean hasCard;

    @Column(name = "has_credit")
    Boolean hasCredit;

    @Column(name = "has_pos")
    Boolean hasPos;

    @Column(name = "today_income", precision = 15, scale = 2)
    BigDecimal todayIncome;

    @Column(name = "today_opening_balance", precision = 15, scale = 2)
    BigDecimal todayOpeningBalance;

    @Column(name = "today_outcome", precision = 15, scale = 2)
    BigDecimal todayOutcome;

    @ToString.Exclude
    @OneToMany(mappedBy = "pashaAccount", orphanRemoval = true, fetch = FetchType.LAZY)
    Set<AccountOperationEntity> accountOperations = new LinkedHashSet<>();

}