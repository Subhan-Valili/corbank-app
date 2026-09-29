package az.corbank.mscorbank.application.service;

import az.corbank.mscorbank.domain.model.Account;
import az.corbank.mscorbank.domain.model.Bank;
import az.corbank.mscorbank.domain.model.Operation;
import az.corbank.mscorbank.application.port.out.BankGateway;
import az.corbank.mscorbank.application.port.out.BankUnavailableException;
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

    BankGateways(List<BankGateway> gateways) {
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

    List<Account> findAllAccountsSafely() {
        List<Account> result = new ArrayList<>();
        for (BankGateway gateway : byBank.values()) {
            try {
                result.addAll(gateway.findAllAccounts());
            } catch (BankUnavailableException e) {
                log.warn("{} hesabları vermədi: {}", gateway.bank(), e.getMessage());
            }
        }
        return result;
    }

    Optional<Account> findAccountSafely(Bank bank, String accountId) {
        Optional<BankGateway> gateway = get(bank);
        if (gateway.isEmpty()) return Optional.empty();
        try {
            return gateway.get().findAccount(accountId);
        } catch (BankUnavailableException e) {
            log.debug("{} bankında hesab tapılmadı ({}): {}", bank, accountId, e.getMessage());
            return Optional.empty();
        }
    }

    List<Operation> findRecentOperationsSafely(BankGateway gateway, int limit) {
        try {
            return gateway.findRecentOperations(limit);
        } catch (BankUnavailableException e) {
            log.warn("{} son əməliyyatları vermədi: {}", gateway.bank(), e.getMessage());
            return List.of();
        }
    }
}
