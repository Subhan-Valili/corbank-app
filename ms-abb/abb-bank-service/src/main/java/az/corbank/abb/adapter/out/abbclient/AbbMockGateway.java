package az.corbank.abb.adapter.out.abbclient;

import az.corbank.abb.adapter.out.persistence.MockAccountJpaEntity;
import az.corbank.abb.adapter.out.persistence.MockAccountJpaRepository;
import az.corbank.abb.adapter.out.persistence.MockOperationJpaEntity;
import az.corbank.abb.adapter.out.persistence.MockOperationJpaRepository;
import az.corbank.abb.application.port.out.AbbBankGateway;
import az.corbank.abb.domain.exception.AbbGatewayException;
import az.corbank.abb.domain.model.AccountStatement;
import az.corbank.abb.domain.model.CorporateAccount;
import az.corbank.abb.domain.model.Direction;
import az.corbank.abb.domain.model.PaymentInstruction;
import az.corbank.abb.domain.model.StatementLine;
import az.corbank.abb.domain.model.StatementQuery;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Stands in for {@link AbbHttpGateway} while there's no real network access to ABB yet
 * (IP whitelisting / VPN / mTLS not sorted out — see the "Connection reset" errors
 * AbbHttpGateway throws as AbbGatewayException when it tries).
 *
 * Everything here is read from Postgres (mock_account / mock_operation tables), NOT from
 * hardcoded Java data — the seed values below only exist to populate those tables once,
 * the first time they're empty. Every subsequent call is a real database read; you can
 * even edit the rows directly in Postgres and this gateway will serve whatever's there.
 *
 * Active only when "abb.api.mock-enabled=true" (see application.yml); flip it to false the
 * moment real ABB access is in place — nothing above the AbbBankGateway port needs to
 * change either way, this and AbbHttpGateway are interchangeable.
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "abb.api", name = "mock-enabled", havingValue = "true")
class AbbMockGateway implements AbbBankGateway {

    private final MockAccountJpaRepository accountRepository;
    private final MockOperationJpaRepository operationRepository;

    AbbMockGateway(MockAccountJpaRepository accountRepository, MockOperationJpaRepository operationRepository) {
        this.accountRepository = accountRepository;
        this.operationRepository = operationRepository;
        log.warn("abb-bank-service is running in MOCK mode (abb.api.mock-enabled=true) — serving "
                + "data from the mock_account/mock_operation Postgres tables, NOT calling the real "
                + "ABB API. Set it to false once real network access + credentials are in place.");
    }

    @PostConstruct
    void seedIfEmpty() {
        if (accountRepository.count() > 0) {
            return;
        }
        log.info("mock_account is empty — seeding demo accounts + sample AZN transactions");

        accountRepository.save(new MockAccountJpaEntity("0000000123456789012", "AZN hesabı", "AZN",
                "AZ21CORB00000000123456789012", new BigDecimal("128450.75")));
        accountRepository.save(new MockAccountJpaEntity("0000000987654321012", "USD hesabı", "USD",
                "AZ21CORB00000000987654321012", new BigDecimal("42800.00")));
        accountRepository.save(new MockAccountJpaEntity("0000000246813579012", "EUR hesabı", "EUR",
                "AZ21CORB00000000246813579012", new BigDecimal("18650.00")));

        String aznAccount = "0000000123456789012";
        operationRepository.save(new MockOperationJpaEntity(aznAccount, "op-1001", "14.04.2025",
                "Qarşı tərəfdən əldə olunan ödəniş", "Caspian Trade MMC", null,
                new BigDecimal("18500.00"), "AZN", "IN"));
        operationRepository.save(new MockOperationJpaEntity(aznAccount, "op-1002", "12.04.2025",
                "Ofis icarəsi", "Caspian Business Center MMC", null,
                new BigDecimal("2400.00"), "AZN", "OUT"));
        operationRepository.save(new MockOperationJpaEntity(aznAccount, "op-1003", "10.04.2025",
                "Əmək haqqı layihəsi üzrə ödəniş", "Caspian HR Services MMC", null,
                new BigDecimal("28400.00"), "AZN", "OUT"));
        operationRepository.save(new MockOperationJpaEntity(aznAccount, "op-1004", "08.04.2025",
                "Qarşı tərəfdən əldə olunan ödəniş", "Araz Logistics MMC", null,
                new BigDecimal("12750.00"), "AZN", "IN"));
    }

    @Override
    public List<CorporateAccount> listAccounts() {
        return accountRepository.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public AccountStatement getStatement(StatementQuery query) {
        MockAccountJpaEntity account = accountRepository.findAll().stream()
                .filter(a -> query.accountNumber().equalsIgnoreCase(a.getAccountNumber())
                        || query.accountNumber().equalsIgnoreCase(a.getIban()))
                .findFirst()
                // Naməlum hesab üçün "ilk hesabı" qaytarmaq başqa bankın (məs. Pasha) hesabına
                // ABB əməliyyatlarını göstərirdi — əvəzinə açıq xəta atırıq (ms-corbank boş siyahı göstərir).
                .orElseThrow(() -> new AbbGatewayException("Mock: account not found: " + query.accountNumber()));

        List<StatementLine> lines = operationRepository
                .findByAccountNumberOrderByIdDesc(account.getAccountNumber()).stream()
                .map(this::toStatementLine)
                .toList();

        return AccountStatement.of(
                account.getAccountNumber(), account.getCurrency(),
                account.getAvailableBalance(), account.getAvailableBalance(),
                1, 1, lines.size(), lines);
    }

    /**
     * Simulates the whole payment ourselves, since in mock mode WE are standing in for
     * the bank: debits fromAccount, inserts an OUT operation, and — if the beneficiary
     * matches one of our own mock accounts (by account number or IBAN) — credits that
     * account too and inserts a matching IN operation. All in one transaction.
     */
    @Override
    @Transactional
    public String submitPayment(PaymentInstruction instruction) {
        MockAccountJpaEntity from = accountRepository.findAll().stream()
                .filter(a -> matches(a, instruction.fromAccountNumber()))
                .findFirst()
                .orElseThrow(() -> new AbbGatewayException("Mock: source account not found: " + instruction.fromAccountNumber()));

        if (from.getAvailableBalance().compareTo(instruction.amount()) < 0) {
            throw new AbbGatewayException("Mock: insufficient balance on " + instruction.fromAccountNumber());
        }

        String reference = "sim-" + UUID.randomUUID().toString().substring(0, 8);
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));

        from.setAvailableBalance(from.getAvailableBalance().subtract(instruction.amount()));
        accountRepository.save(from);
        operationRepository.save(new MockOperationJpaEntity(from.getAccountNumber(), reference, today,
                instruction.description(), instruction.beneficiaryName(), null,
                instruction.amount(), from.getCurrency(), "OUT"));

        Optional<MockAccountJpaEntity> beneficiary = accountRepository.findAll().stream()
                .filter(a -> matches(a, instruction.beneficiaryAccountNumber()))
                .findFirst();

        beneficiary.ifPresent(to -> {
            // Valyutalar fərqlidirsə (AZN -> USD) alıcıya konvertasiya olunmuş məbləğ düşür; əks halda
            // 131 AZN, USD hesabına "131 USD" kimi düşürdü.
            BigDecimal credit = instruction.creditAmount() != null && instruction.creditAmount().signum() > 0
                    ? instruction.creditAmount() : instruction.amount();
            to.setAvailableBalance(to.getAvailableBalance().add(credit));
            accountRepository.save(to);
            operationRepository.save(new MockOperationJpaEntity(to.getAccountNumber(), reference + "-in", today,
                    instruction.description(), from.getName(), null,
                    credit, to.getCurrency(), "IN"));
        });

        log.info("Mock payment {} completed: {} -> {} ({} {})", reference, instruction.fromAccountNumber(),
                instruction.beneficiaryAccountNumber(), instruction.amount(), instruction.currency());
        return reference;
    }

    private boolean matches(MockAccountJpaEntity account, String accountNumber) {
        return accountNumber != null
                && (accountNumber.equalsIgnoreCase(account.getAccountNumber()) || accountNumber.equalsIgnoreCase(account.getIban()));
    }

    private CorporateAccount toDomain(MockAccountJpaEntity e) {
        return new CorporateAccount(e.getName(), e.getCurrency(), e.getAccountNumber(), e.getIban(), e.getAvailableBalance());
    }

    private StatementLine toStatementLine(MockOperationJpaEntity e) {
        return new StatementLine(e.getAccountNumber(), e.getReference(), e.getTransactionDate(), e.getDescription(),
                e.getCounterparty(), e.getBeneficiaryTin(), e.getAmount(), e.getCurrency(),
                Direction.valueOf(e.getDirection()));
    }
}
