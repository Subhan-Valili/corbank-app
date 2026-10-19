package az.corbank.mscorbank.application.service;

import az.corbank.mscorbank.domain.model.Account;
import az.corbank.mscorbank.domain.model.Bank;
import az.corbank.mscorbank.domain.model.Operation;
import az.corbank.mscorbank.application.port.out.BankGateway;
import az.corbank.mscorbank.application.port.out.BankUnavailableException;
import az.corbank.mscorbank.application.port.out.CurrentCustomerPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Registry of the bank adapters plus the "an unreachable bank must not break the others" policy.
 * Every *Safely method logs and degrades to empty instead of propagating BankUnavailableException.
 */
@Component
@Slf4j
class BankGateways {

    private final Map<Bank, BankGateway> byBank = new EnumMap<>(Bank.class);

    private final CurrentCustomerPort customer;

    BankGateways(List<BankGateway> gateways, CurrentCustomerPort customer) {
        this.customer = customer;
        gateways.forEach(gateway -> byBank.put(gateway.bank(), gateway));
    }

    /** In Bank declaration order. */
    List<BankGateway> all() {
        return new ArrayList<>(byBank.values());
    }

    Optional<BankGateway> get(Bank bank) {
        return Optional.ofNullable(byBank.get(bank));
    }

    BankGateway require(Bank bank) {
        return get(bank).orElseThrow(() -> new IllegalStateException("No gateway configured for bank " + bank));
    }

    /** Yalnız CARİ istifadəçinin hesabları (qoşduğu banklardan). Heç nə qoşmayıbsa boş siyahı. */
    List<Account> findAllAccountsSafely() {
        // Hər bank öz etibarnaməsini özü seçir: ABB və Paşa saxlanmış API Key ilə.
        // Etibarnamə yoxdursa adapter boş siyahı qaytarır.
        String pin = customer.currentPin().orElse(null);

        List<Account> result = new ArrayList<>();
        for (BankGateway gateway : byBank.values()) {
            try {
                result.addAll(gateway.findAllAccounts(pin));
            } catch (BankUnavailableException e) {
                log.warn("{} hesabları vermədi: {}", gateway.bank(), e.getMessage());
            }
        }
        return result;
    }

    /**
     * Hesabı yalnız cari istifadəçinin öz hesabları arasında axtarır (id və ya IBAN ilə).
     * Başqasının hesabı = tapılmadı. Bütün hesab/əməliyyat/ödəniş əməliyyatları bundan keçməlidir.
     */
    Optional<Account> findOwnedAccount(String accountId) {
        if (accountId == null || accountId.isBlank()) return Optional.empty();
        return findAllAccountsSafely().stream()
                .filter(a -> accountId.equalsIgnoreCase(a.id()) || accountId.equalsIgnoreCase(a.iban()))
                .findFirst();
    }

    List<Operation> findRecentOperationsSafely(BankGateway gateway, int limit) {
        Optional<String> pin = customer.currentPin();
        if (gateway.supportsApiKey()) {
            if (customer.currentApiKey(gateway.bank()).isEmpty()) return List.of();
        } else if (pin.isEmpty()) {
            return List.of();
        }
        try {
            return gateway.findRecentOperations(pin.orElse(null), limit);
        } catch (BankUnavailableException e) {
            log.warn("{} son əməliyyatları vermədi: {}", gateway.bank(), e.getMessage());
            return List.of();
        }
    }
}
