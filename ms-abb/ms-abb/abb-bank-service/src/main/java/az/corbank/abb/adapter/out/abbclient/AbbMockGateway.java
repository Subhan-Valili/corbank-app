package az.corbank.abb.adapter.out.abbclient;

import az.corbank.abb.adapter.out.persistence.MockAccountJpaEntity;
import az.corbank.abb.adapter.out.persistence.MockAccountJpaRepository;
import az.corbank.abb.adapter.out.persistence.MockOperationJpaEntity;
import az.corbank.abb.adapter.out.persistence.MockOperationJpaRepository;
import az.corbank.abb.application.port.out.AbbBankGateway;
import az.corbank.abb.domain.exception.AbbGatewayException;
import az.corbank.abb.domain.model.AccountStatement;
import az.corbank.abb.domain.model.CorporateAccount;
import az.corbank.abb.domain.model.CustomerIdentity;
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
        log.info("mock_account is empty — seeding demo ABB accounts + sample transactions");

        // Pasha (ms-pasha) ilə QARIŞIQ: kim hansı bankda hesabı var
        //   Subhan Vəlili   — Pasha + ABB      (PIN 5SV1A23)
        //   Rasul Kərimov   — yalnız Pasha     (ABB-də YOXDUR, burada seed olunmur)
        //   Fərid Abbasov   — yalnız ABB       (PIN 7FA3C45)
        //   İmran Yusubov   — Pasha + ABB      (PIN 8IY4D56)
        // Tarixlər bu günə nisbətidir (corbank default 90 günlük pəncərə istifadə edir).

        // ---- Subhan Vəlili  ----
        String subhanAzn = "0000000410000000011";
        String subhanEur = "0000000410000000012";
        saveAccount(subhanAzn, "5SV1A23", "+994501110001", "AZN hesabı", "AZN", "18740.50");
        saveAccount(subhanEur, "5SV1A23", "+994501110001", "EUR hesabı", "EUR", "2650.00");

        saveOp(subhanAzn, "abb-s-1001", 2,  "Əmək haqqı",                      "Caspian Tech MMC",        "3200.00", "AZN", "IN");
        saveOp(subhanAzn, "abb-s-1002", 4,  "Kommunal xidmətlər",              "Azərişıq ASC",            "86.40",   "AZN", "OUT");
        saveOp(subhanAzn, "abb-s-1003", 7,  "Mobil rabitə ödənişi",            "Azercell MMC",            "25.00",   "AZN", "OUT");
        saveOp(subhanAzn, "abb-s-1004", 11, "Frilans layihə ödənişi",          "Baku Digital Agency MMC", "1450.00", "AZN", "IN");
        saveOp(subhanAzn, "abb-s-1005", 15, "Market alış-verişi",              "Bravo Supermarket",       "112.75",  "AZN", "OUT");
        saveOp(subhanAzn, "abb-s-1006", 21, "Ofis avadanlığı",                 "Irshad Electronics",      "980.00",  "AZN", "OUT");
        saveOp(subhanAzn, "abb-s-1007", 33, "Əmək haqqı",                      "Caspian Tech MMC",        "3200.00", "AZN", "IN");
        saveOp(subhanEur, "abb-s-2001", 6,  "Xarici müştəridən ödəniş",        "Berlin Software GmbH",    "850.00",  "EUR", "IN");
        saveOp(subhanEur, "abb-s-2002", 19, "Onlayn xidmət abunəliyi",         "Adobe Systems",           "54.99",   "EUR", "OUT");

        // ---- Fərid Abbasov (yalnız ABB) ----
        String faridAzn = "0000000420000000021";
        String faridUsd = "0000000420000000022";
        saveAccount(faridAzn, "7FA3C45", "+994501110003", "AZN hesabı", "AZN", "9320.00");
        saveAccount(faridUsd, "7FA3C45", "+994501110003", "USD hesabı", "USD", "4100.75");

        saveOp(faridAzn, "abb-f-1001", 1,  "Əmək haqqı",                       "Araz Logistics MMC",      "2100.00", "AZN", "IN");
        saveOp(faridAzn, "abb-f-1002", 3,  "Kirayə haqqı",                     "Elnur Hüseynov",          "650.00",  "AZN", "OUT");
        saveOp(faridAzn, "abb-f-1003", 8,  "Nəqliyyat xərci",                  "Bakı Taksi MMC",          "38.50",   "AZN", "OUT");
        saveOp(faridAzn, "abb-f-1004", 12, "Mal satışından daxilolma",         "Gəncə Ticarət MMC",       "1780.00", "AZN", "IN");
        saveOp(faridAzn, "abb-f-1005", 18, "Yanacaq",                          "Socar Petroleum",         "95.00",   "AZN", "OUT");
        saveOp(faridAzn, "abb-f-1006", 27, "Sığorta haqqı",                    "Paşa Sığorta ASC",        "420.00",  "AZN", "OUT");
        saveOp(faridUsd, "abb-f-2001", 5,  "Xaricdən köçürmə",                 "Istanbul Trade Ltd",      "1200.00", "USD", "IN");
        saveOp(faridUsd, "abb-f-2002", 22, "Avadanlıq alışı",                  "Dell Technologies",       "640.00",  "USD", "OUT");

        // ---- İmran Yusubov ----
        String imranAzn = "0000000430000000031";
        String imranUsd = "0000000430000000032";
        saveAccount(imranAzn, "8IY4D56", "+994501110004", "AZN hesabı", "AZN", "15400.00");
        saveAccount(imranUsd, "8IY4D56", "+994501110004", "USD hesabı", "USD", "760.00");

        saveOp(imranAzn, "abb-i-1001", 2,  "Müştəridən ödəniş",                "Khazar Trading MMC",      "6200.00", "AZN", "IN");
        saveOp(imranAzn, "abb-i-1002", 5,  "Podratçı ödənişi",                 "Sumqayıt Tikinti MMC",    "2850.00", "AZN", "OUT");
        saveOp(imranAzn, "abb-i-1003", 9,  "İnternet və telefon",              "Delta Telecom",           "49.90",   "AZN", "OUT");
        saveOp(imranAzn, "abb-i-1004", 14, "Müştəridən ödəniş",                "Shirvan Agro MMC",        "3900.00", "AZN", "IN");
        saveOp(imranAzn, "abb-i-1005", 20, "Vergi ödənişi",                    "Dövlət Vergi Xidməti",    "1320.00", "AZN", "OUT");
        saveOp(imranAzn, "abb-i-1006", 29, "Ofis icarəsi",                     "Baku White City MMC",     "1800.00", "AZN", "OUT");
        saveOp(imranUsd, "abb-i-2001", 10, "Konsaltinq xidməti ödənişi",       "Dubai Partners FZE",      "900.00",  "USD", "IN");
        saveOp(imranUsd, "abb-i-2002", 25, "Domen və hostinq",                 "Namecheap Inc",           "140.00",  "USD", "OUT");
    }

    private void saveAccount(String accountNumber, String pin, String mobile, String name, String currency, String balance) {
        // IBAN: AZ21ABBA + 20 rəqəm (0 + 19 rəqəmli hesab nömrəsi)
        String iban = "AZ21ABBA0" + accountNumber;
        accountRepository.save(new MockAccountJpaEntity(accountNumber, pin, mobile, name, currency, iban, new BigDecimal(balance)));
    }

    private void saveOp(String accountNumber, String reference, int daysAgo, String description,
                        String counterparty, String amount, String currency, String direction) {
        String date = LocalDate.now().minusDays(daysAgo).format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        operationRepository.save(new MockOperationJpaEntity(accountNumber, reference, date, description,
                counterparty, null, new BigDecimal(amount), currency, direction));
    }

    @Override
    public List<CorporateAccount> listAccounts() {
        return accountRepository.findAll().stream().map(this::toDomain).toList();
    }

    /** PIN + mobile both given -> both must match; otherwise whichever one was given. */
    @Override
    public List<CorporateAccount> listAccounts(CustomerIdentity identity) {
        List<MockAccountJpaEntity> found;
        if (identity.pin() != null && identity.mobileNumber() != null) {
            found = accountRepository.findByPinCodeAndMobileNumber(identity.pin(), identity.mobileNumber());
        } else if (identity.pin() != null) {
            found = accountRepository.findByPinCode(identity.pin());
        } else {
            found = accountRepository.findByMobileNumber(identity.mobileNumber());
        }
        return found.stream().map(this::toDomain).toList();
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
