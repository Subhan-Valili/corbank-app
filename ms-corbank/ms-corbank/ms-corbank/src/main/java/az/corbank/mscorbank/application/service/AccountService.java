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
        // yalnız cari istifadəçinin öz hesabları arasında
        return banks.findOwnedAccount(accountId);
    }
}
