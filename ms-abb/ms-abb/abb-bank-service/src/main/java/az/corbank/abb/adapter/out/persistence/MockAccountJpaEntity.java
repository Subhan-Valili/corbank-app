package az.corbank.abb.adapter.out.persistence;

import jakarta.persistence.*;
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
@Table(name = "mock_account",
        indexes = {
                @Index(name = "idx_mock_account_pin", columnList = "pinCode"),
                @Index(name = "idx_mock_account_mobile", columnList = "mobileNumber")
        })
@Getter
@Setter
@NoArgsConstructor
public class MockAccountJpaEntity {

    @Id
    private String accountNumber;

    /** Customer's PIN. Not unique: one customer can own several accounts. */
    private String pinCode;

    /** Customer's mobile number, E.164 format, e.g. +994501234567. Not unique for the same reason. */
    @Column(nullable = false)
    private String mobileNumber;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String currency;

    private String iban;

    @Column(nullable = false)
    private BigDecimal availableBalance;

    public MockAccountJpaEntity(String accountNumber, String pinCode, String mobileNumber,
                                String name, String currency, String iban,
                                BigDecimal availableBalance) {
        this.accountNumber = accountNumber;
        this.pinCode = pinCode;
        this.mobileNumber = mobileNumber;
        this.name = name;
        this.currency = currency;
        this.iban = iban;
        this.availableBalance = availableBalance;
    }
}
