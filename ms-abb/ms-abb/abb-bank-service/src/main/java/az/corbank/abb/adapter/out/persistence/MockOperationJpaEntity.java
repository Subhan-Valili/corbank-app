package az.corbank.abb.adapter.out.persistence;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Demo/mock statement line, read by AbbMockGateway.getStatement(). Kept as its own table,
 * separate from operation_history (which caches whatever a LIVE call to ABB actually
 * returned) — these are two different concepts even though the row shape is similar:
 * this one is fixed seed data, that one is a real cache that grows over time.
 */
@Entity
@Table(name = "mock_operation")
@Getter
@Setter
@NoArgsConstructor
public class MockOperationJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String accountNumber;

    @Column(nullable = false, unique = true)
    private String reference;

    @Column(nullable = false)
    private String transactionDate; // dd.MM.yyyy, same raw format ABB itself would use

    private String description;
    private String counterparty;
    private String beneficiaryTin;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false)
    private String currency;

    /** "IN" | "OUT" */
    @Column(nullable = false)
    private String direction;

    public MockOperationJpaEntity(String accountNumber, String reference, String transactionDate,
                                    String description, String counterparty, String beneficiaryTin,
                                    BigDecimal amount, String currency, String direction) {
        this.accountNumber = accountNumber;
        this.reference = reference;
        this.transactionDate = transactionDate;
        this.description = description;
        this.counterparty = counterparty;
        this.beneficiaryTin = beneficiaryTin;
        this.amount = amount;
        this.currency = currency;
        this.direction = direction;
    }
}
