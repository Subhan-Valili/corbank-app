package az.corbank.mscorbank.application.service;

import az.corbank.mscorbank.domain.model.Account;
import az.corbank.mscorbank.domain.model.Bank;
import az.corbank.mscorbank.application.port.in.GetAccountUseCase;
import az.corbank.mscorbank.application.port.in.ListAccountsUseCase;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
class AccountService implements ListAccountsUseCase, GetAccountUseCase {

    /**
     * ABB is asked first: its account id IS its IBAN, whereas PASHA may answer an unknown id with
     * some default account, which would wrongly shadow an ABB (e.g. USD/EUR) account.
     */
    private static final List<Bank> LOOKUP_ORDER = List.of(Bank.ABB, Bank.PASHA);

    private final BankGateways banks;

    AccountService(BankGateways banks) {
        this.banks = banks;
    }

    @Override
    public List<Account> listAccounts(Bank bank) {
        List<Account> all = banks.findAllAccountsSafely();
        if (bank == null) return all;
        List<Account> filtered = all.stream().filter(a -> a.bank() == bank).toList();
        return filtered.isEmpty() ? all : filtered;
    }

    @Override
    public Optional<Account> getAccount(String accountId) {
        for (Bank bank : LOOKUP_ORDER) {
            Optional<Account> account = banks.findAccountSafely(bank, accountId);
            if (account.isPresent()) return account;
        }
        return Optional.empty();
    }
}
