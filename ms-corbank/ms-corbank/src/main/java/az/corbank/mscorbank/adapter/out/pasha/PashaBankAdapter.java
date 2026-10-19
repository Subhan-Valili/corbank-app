package az.corbank.mscorbank.adapter.out.pasha;

import az.corbank.mscorbank.domain.model.Account;
import az.corbank.mscorbank.domain.model.AccountStatus;
import az.corbank.mscorbank.domain.model.Bank;
import az.corbank.mscorbank.domain.model.Operation;
import az.corbank.mscorbank.domain.model.OperationDirection;
import az.corbank.mscorbank.domain.model.OperationStatus;
import az.corbank.mscorbank.domain.model.PaymentCommand;
import az.corbank.mscorbank.domain.model.PaymentResult;
import az.corbank.mscorbank.application.port.out.BankGateway;
import az.corbank.mscorbank.application.exception.InvalidApiKeyException;
import az.corbank.mscorbank.application.port.out.BankUnavailableException;
import az.corbank.mscorbank.application.port.out.CurrentCustomerPort;
import az.corbank.mscorbank.adapter.out.pasha.client.AccountOperationFeignClient;
import az.corbank.mscorbank.adapter.out.pasha.client.BulkControllerFeignClient;
import az.corbank.mscorbank.adapter.out.pasha.client.PashaAccountFeignClient;
import az.corbank.mscorbank.adapter.out.pasha.dto.account.AccountOperationDto;
import az.corbank.mscorbank.adapter.out.pasha.dto.account.PashaAccountResponseDto;
import az.corbank.mscorbank.adapter.out.pasha.dto.bulk.CreateBulkPaymentRequest;
import az.corbank.mscorbank.adapter.out.pasha.dto.bulk.CreateBulkPaymentResponse;
import az.corbank.mscorbank.adapter.out.pasha.dto.bulk.PayeeDto;
import az.corbank.mscorbank.adapter.out.pasha.dto.bulk.PayerDto;
import az.corbank.mscorbank.adapter.out.pasha.dto.bulk.PaymentRequest;
import az.corbank.mscorbank.adapter.out.pasha.enums.OperationType;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/** Outbound adapter: PASHA Bank (ms-pasha) behind the {@link BankGateway} port. */
@Component
@Slf4j
class PashaBankAdapter implements BankGateway {

    private final PashaAccountFeignClient accountClient;
    private final AccountOperationFeignClient operationClient;
    private final BulkControllerFeignClient bulkClient;
    private final CurrentCustomerPort customer;

    PashaBankAdapter(PashaAccountFeignClient accountClient,
                     AccountOperationFeignClient operationClient,
                     BulkControllerFeignClient bulkClient,
                     CurrentCustomerPort customer) {
        this.accountClient = accountClient;
        this.operationClient = operationClient;
        this.bulkClient = bulkClient;
        this.customer = customer;
    }

    @Override
    public Bank bank() {
        return Bank.PASHA;
    }

    /** Paşa istifadəçinin saxlanmış Open API Key-i ilə işləyir; PIN parametri istifadə olunmur. */
    @Override
    public List<Account> findAllAccounts(String pin) {
        Optional<String> apiKey = customer.currentApiKey(Bank.PASHA);
        if (apiKey.isEmpty()) return List.of();
        try {
            return accountClient.findAccountsByApiKey(apiKey.get(), null).stream().map(this::toAccount).toList();
        } catch (FeignException.NotFound e) {
            return List.of();
        } catch (FeignException e) {
            throw new BankUnavailableException(Bank.PASHA, e.getMessage(), e);
        }
    }

    @Override
    public boolean supportsApiKey() {
        return true;
    }

    /** 400/401/403/404 = açar yanlışdır; qalan xətalar (5xx, timeout) = bank əlçatmazdır. */
    @Override
    public List<Account> findAccountsByApiKey(String apiKey) {
        try {
            List<PashaAccountResponseDto> accounts = accountClient.findAccountsByApiKey(apiKey, null);
            return accounts == null ? List.of() : accounts.stream().map(this::toAccount).toList();
        } catch (FeignException e) {
            int status = e.status();
            if (status == 400 || status == 401 || status == 403 || status == 404) {
                throw new InvalidApiKeyException("PASHA Bank bu Open API Key-i qəbul etmədi");
            }
            throw new BankUnavailableException(Bank.PASHA, e.getMessage(), e);
        }
    }

    @Override
    public List<Operation> findOperations(String accountId, LocalDate from, LocalDate to) {
        try {
            return operationClient.getOperations(accountId, from, to).data().stream().map(o -> toOperation(o, accountId)).toList();
        } catch (FeignException e) {
            throw new BankUnavailableException(Bank.PASHA, e.getMessage(), e);
        }
    }

    @Override
    public PaymentResult submitPayment(PaymentCommand command) {
        PaymentRequest payment = PaymentRequest.builder()
                .type("TRANSFER")
                .amount(command.amount())
                .description(command.description())
                .payer(PayerDto.builder().accountNumber(command.fromAccountId()).build())
                .payee(PayeeDto.builder()
                        .accountNumber(command.beneficiaryIban())
                        .name(command.beneficiaryName())
                        .build())
                .urgent(false)
                .build();

        CreateBulkPaymentRequest bulkRequest = CreateBulkPaymentRequest.builder()
                .bulkDescription(command.description())
                .payments(List.of(payment))
                .build();

        try {
            CreateBulkPaymentResponse response = bulkClient.createBulkPayments(bulkRequest);
            return PaymentResult.completed("Ödəniş ms-pasha-ya göndərildi", response != null ? response.getBulkId() : null);
        } catch (FeignException e) {
            log.warn("ms-pasha ödəniş sorğusunu rədd etdi: {}", e.getMessage());
            return PaymentResult.rejected("ms-pasha ödənişi qəbul etmədi: " + e.getMessage());
        }
    }

    // ---------------------------------------------------------------- mapping (PASHA wire format -> domain)

    private Account toAccount(PashaAccountResponseDto a) {
        BigDecimal balance = a.availableBalance() != null ? a.availableBalance() : a.currentBalance();
        return new Account(a.accountId(), displayName(a), balance, a.currency(), a.iban(), toStatus(a.accountStatus()), Bank.PASHA);
    }

    private String displayName(PashaAccountResponseDto a) {
        String type = a.accountType() != null ? a.accountType() : "Cari";
        String currency = a.currency() != null ? a.currency() : "";
        return "%s hesabı (%s)".formatted(currency, type).trim();
    }

    private AccountStatus toStatus(String pashaStatus) {
        if (pashaStatus == null) return AccountStatus.ACTIVE;
        return switch (pashaStatus.toUpperCase()) {
            case "ACTIVE", "OPEN" -> AccountStatus.ACTIVE;
            case "BLOCKED", "FROZEN" -> AccountStatus.BLOCKED;
            case "CLOSED" -> AccountStatus.CLOSED;
            default -> AccountStatus.OTHER;
        };
    }

    private Operation toOperation(AccountOperationDto op, String accountId) {
        boolean credit = op.type() == OperationType.CREDIT;
        return new Operation(
                op.id(),
                op.date() != null ? op.date().toLocalDate() : null,
                op.counterparty() != null ? op.counterparty().name() : "—",
                op.description(),
                op.amount() != null ? op.amount().value() : BigDecimal.ZERO,
                op.amount() != null ? op.amount().currencyCode() : null,
                credit ? OperationDirection.IN : OperationDirection.OUT,
                OperationStatus.COMPLETED,
                accountId,
                Bank.PASHA);
    }
}
