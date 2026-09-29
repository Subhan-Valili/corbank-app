package az.corbank.mscorbank.adapter.out.abb;

import az.corbank.mscorbank.domain.model.Account;
import az.corbank.mscorbank.domain.model.AccountStatus;
import az.corbank.mscorbank.domain.model.Bank;
import az.corbank.mscorbank.domain.model.Operation;
import az.corbank.mscorbank.domain.model.OperationDirection;
import az.corbank.mscorbank.domain.model.OperationStatus;
import az.corbank.mscorbank.domain.model.PaymentCommand;
import az.corbank.mscorbank.domain.model.PaymentResult;
import az.corbank.mscorbank.application.port.out.BankGateway;
import az.corbank.mscorbank.application.port.out.BankUnavailableException;
import az.corbank.mscorbank.adapter.out.abb.client.AbbAccountFeignClient;
import az.corbank.mscorbank.adapter.out.abb.dto.AbbAccountResponseDto;
import az.corbank.mscorbank.adapter.out.abb.dto.AbbOperationLineDto;
import az.corbank.mscorbank.adapter.out.abb.dto.AbbPaymentRequestDto;
import az.corbank.mscorbank.adapter.out.abb.dto.AbbPaymentResponseDto;
import az.corbank.mscorbank.adapter.out.abb.dto.AbbStatementResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;


@Component
@Slf4j
class AbbBankAdapter implements BankGateway {

    private static final DateTimeFormatter QUERY_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter ABB_DATE = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private final AbbAccountFeignClient client;

    AbbBankAdapter(AbbAccountFeignClient client) {
        this.client = client;
    }

    @Override
    public Bank bank() {
        return Bank.ABB;
    }

    @Override
    public List<Account> findAllAccounts() {
        try {
            return client.findAllAccounts().stream().map(this::toAccount).toList();
        } catch (Exception e) {
            throw new BankUnavailableException(Bank.ABB, e.getMessage(), e);
        }
    }

    /** An ABB account is identified by its IBAN (falling back to the account number). */
    @Override
    public Optional<Account> findAccount(String accountId) {
        if (accountId == null) return Optional.empty();
        return findAllAccounts().stream()
                .filter(a -> accountId.equalsIgnoreCase(a.id()) || accountId.equalsIgnoreCase(a.iban()))
                .findFirst();
    }

    @Override
    public List<Operation> findOperations(String accountId, LocalDate fromDate, LocalDate toDate) {
        try {
            LocalDate to = toDate != null ? toDate : LocalDate.now();
            LocalDate from = fromDate != null ? fromDate : to.minusDays(90);
            AbbStatementResponseDto statement = client.getStatement(
                    accountId, from.format(QUERY_DATE), to.format(QUERY_DATE), 1, 200);
            // ABB (and its mock) do not always honour the date range — filter here so both banks
            // behave the same for the same UI filter.
            return statement.transactions().stream()
                    .map(this::toOperation)
                    .filter(op -> withinRange(op.date(), from, to))
                    .toList();
        } catch (Exception e) {
            throw new BankUnavailableException(Bank.ABB, e.getMessage(), e);
        }
    }

    @Override
    public List<Operation> findRecentOperations(int limit) {
        try {
            return client.getRecentHistory(limit).stream().map(this::toOperation).toList();
        } catch (Exception e) {
            throw new BankUnavailableException(Bank.ABB, e.getMessage(), e);
        }
    }

    @Override
    public PaymentResult submitPayment(PaymentCommand command) {
        AbbPaymentRequestDto request = AbbPaymentRequestDto.builder()
                .fromAccountNumber(command.fromAccountId())
                .beneficiaryAccountNumber(command.beneficiaryIban())
                .beneficiaryName(command.beneficiaryName())
                .amount(command.amount())
                .currency(command.currency())
                .description(command.description())
                .creditAmount(command.creditAmount())
                .creditCurrency(command.creditCurrency())
                .build();
        try {
            AbbPaymentResponseDto response = client.submitPayment(request);
            return "COMPLETED".equalsIgnoreCase(response.status())
                    ? PaymentResult.completed(response.message(), null)
                    : PaymentResult.rejected(response.message());
        } catch (Exception e) {
            log.warn("ms-abb-bank ödəniş sorğusunu qəbul etmədi: {}", e.getMessage());
            return PaymentResult.rejected("ms-abb-bank ödənişi qəbul etmədi: " + e.getMessage());
        }
    }

    // ---------------------------------------------------------------- mapping (ABB wire format -> domain)

    private Account toAccount(AbbAccountResponseDto a) {
        String id = a.iban() != null ? a.iban() : a.accountNumber();
        String name = a.name() != null ? a.name()
                : (a.currency() != null ? a.currency() + " hesabı (ABB)" : "ABB hesabı");
        return new Account(id, name, a.availableBalance(), a.currency(), a.iban(), AccountStatus.ACTIVE, Bank.ABB);
    }

    private Operation toOperation(AbbOperationLineDto op) {
        return new Operation(
                op.reference(),
                parseDate(op.date()),
                op.counterparty() != null ? op.counterparty() : "—",
                op.description(),
                op.amount() != null ? op.amount() : BigDecimal.ZERO,
                op.currency(),
                "IN".equalsIgnoreCase(op.direction()) ? OperationDirection.IN : OperationDirection.OUT,
                OperationStatus.COMPLETED);
    }

    /** ABB uses dd.MM.yyyy; ISO is accepted too. Anything else becomes null (unknown date). */
    private LocalDate parseDate(String raw) {
        if (raw == null || raw.isBlank()) return null;
        try {
            return LocalDate.parse(raw, ABB_DATE);
        } catch (DateTimeParseException notAbbFormat) {
            try {
                return LocalDate.parse(raw);
            } catch (DateTimeParseException notIsoEither) {
                return null;
            }
        }
    }

    /** Operations with an unknown date are kept: better to show a row than to lose it. */
    private boolean withinRange(LocalDate date, LocalDate from, LocalDate to) {
        return date == null || (!date.isBefore(from) && !date.isAfter(to));
    }
}
