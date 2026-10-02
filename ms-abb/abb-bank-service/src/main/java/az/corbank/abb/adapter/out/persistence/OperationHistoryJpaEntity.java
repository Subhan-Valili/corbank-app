package az.corbank.abb.adapter.out.persistence;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * One persisted operation/payment history line. Unique on (accountNumber, reference) so
 * re-fetching an overlapping statement date range upserts rather than duplicates rows.
 */
@Entity
@Table(name = "operation_history",
        uniqueConstraints = @UniqueConstraint(columnNames = {"accountNumber", "reference"}))
@Getter
@Setter
@NoArgsConstructor
class OperationHistoryJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String accountNumber;

    @Column(nullable = false)
    private String reference;

    /** Raw dd.MM.yyyy string exactly as ABB reports it — kept for display fidelity. */
    private String transactionDateRaw;

    /**
     * Parsed transaction date, used for chronological sorting ("recent operations" means
     * recent by transaction date, not by when we happened to fetch it). Nullable: if ABB
     * ever sends a date we can't parse, we still store the row, it just sorts last.
     */
    private LocalDate transactionDate;

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

    @Column(nullable = false)
    private Instant fetchedAt;
}
