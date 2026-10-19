package az.corbank.mscorbank.adapter.in.web.passthrough;

import az.corbank.mscorbank.application.exception.AccountNotFoundException;
import az.corbank.mscorbank.application.port.in.ListAccountsUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Proxy ödəniş endpoint-lərində "ödəyən hesab" cari istifadəçiyə aid olmalıdır. */
@Component
@RequiredArgsConstructor
class PayerOwnershipGuard {

    private final ListAccountsUseCase listAccounts;

    void requireOwned(String accountNumber) {
        boolean owned = accountNumber != null && listAccounts.listAccounts(null).stream()
                .anyMatch(a -> accountNumber.equalsIgnoreCase(a.id()) || accountNumber.equalsIgnoreCase(a.iban()));
        if (!owned) throw new AccountNotFoundException(String.valueOf(accountNumber));
    }
}
