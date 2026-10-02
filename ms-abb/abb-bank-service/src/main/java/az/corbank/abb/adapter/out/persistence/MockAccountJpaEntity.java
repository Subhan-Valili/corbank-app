package az.corbank.abb.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * Demo/mock account, read by AbbMockGateway while there's no real network access to ABB
 * (see AbbMockGateway's javadoc). Seeded once when empty; from then on this is a real
 * Postgres table like everything else here — no hardcoded data in Java after that point.
 */
@Entity
@Table(name = "mock_account")
@Getter
@Setter
@NoArgsConstructor
public class MockAccountJpaEntity {

    @Id
    private String accountNumber;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String currency;

    private String iban;

    @Column(nullable = false)
    private BigDecimal availableBalance;

    public MockAccountJpaEntity(String accountNumber, String name, String currency, String iban, BigDecimal availableBalance) {
        this.accountNumber = accountNumber;
        this.name = name;
        this.currency = currency;
        this.iban = iban;
        this.availableBalance = availableBalance;
    }
}
