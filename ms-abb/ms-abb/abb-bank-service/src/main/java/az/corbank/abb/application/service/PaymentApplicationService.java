package az.corbank.abb.application.service;

import az.corbank.abb.application.port.in.SubmitPaymentUseCase;
import az.corbank.abb.application.port.out.AbbBankGateway;
import az.corbank.abb.domain.exception.AbbGatewayException;
import az.corbank.abb.domain.model.CorporateAccount;
import az.corbank.abb.domain.model.PaymentInstruction;
import az.corbank.abb.domain.model.PaymentOutcome;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Checks that the source account exists and has sufficient balance BEFORE calling the
 * gateway — this business rule applies the same way whether the gateway is the real ABB
 * API or the mock (both just execute the transfer once we know it's valid), so it lives
 * here rather than being duplicated in each gateway implementation.
 */
public class PaymentApplicationService implements SubmitPaymentUseCase {

    private static final Logger log = LoggerFactory.getLogger(PaymentApplicationService.class);

    private final AbbBankGateway gateway;

    public PaymentApplicationService(AbbBankGateway gateway) {
        this.gateway = gateway;
    }

    @Override
    public PaymentOutcome submit(PaymentInstruction instruction) {
        if (instruction.amount() == null || instruction.amount().signum() <= 0) {
            return PaymentOutcome.rejected("Məbləğ düzgün deyil.");
        }

        List<CorporateAccount> accounts = gateway.listAccounts();
        Optional<CorporateAccount> from = accounts.stream()
                .filter(a -> matches(a, instruction.fromAccountNumber()))
                .findFirst();

        if (from.isEmpty()) {
            return PaymentOutcome.rejected("Mənbə hesabı tapılmadı.");
        }

        BigDecimal balance = from.get().availableBalance();
        if (balance == null || balance.compareTo(instruction.amount()) < 0) {
            return PaymentOutcome.rejected("Hesabda kifayət qədər vəsait yoxdur.");
        }

        try {
            String reference = gateway.submitPayment(instruction);
            return PaymentOutcome.completed(reference);
        } catch (AbbGatewayException e) {
            log.warn("ABB ödəniş sorğusunu rədd etdi: {}", e.getMessage());
            return PaymentOutcome.rejected("ABB ödənişi qəbul etmədi: " + e.getMessage());
        }
    }

    private boolean matches(CorporateAccount account, String accountNumber) {
        return accountNumber.equalsIgnoreCase(account.accountNumber()) || accountNumber.equalsIgnoreCase(account.iban());
    }
}
