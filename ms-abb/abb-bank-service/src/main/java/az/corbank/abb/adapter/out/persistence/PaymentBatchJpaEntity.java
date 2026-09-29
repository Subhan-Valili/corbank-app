package az.corbank.abb.adapter.out.persistence;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "payment_batch")
@Getter
@Setter
@NoArgsConstructor
class PaymentBatchJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String batchNumber;

    private String externalReference;

    @Column(nullable = false)
    private String batchType;

    @Column(nullable = false)
    private String lastKnownStatus;

    private String lastKnownStatusDescription;

    @Column(nullable = false)
    private Instant submittedAt;

    private Instant lastCheckedAt;
}
