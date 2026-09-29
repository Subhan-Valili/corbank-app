package az.corbank.abb.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "account_snapshot")
@Getter
@Setter
@NoArgsConstructor
class AccountSnapshotJpaEntity {

    @Id
    private String accountNumber;

    @Column(nullable = false)
    private String currency;

    @Column(nullable = false)
    private BigDecimal availableBalance;

    @Column(nullable = false)
    private Instant fetchedAt;
}
