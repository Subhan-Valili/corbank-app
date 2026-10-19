package az.corbank.mscorbank.application.port.in;

import az.corbank.mscorbank.domain.model.Account;
import az.corbank.mscorbank.domain.model.Bank;

import java.util.List;

public interface ListAccountsUseCase {

    /** bank == null, or a bank without accounts, yields the accounts of all banks. */
    List<Account> listAccounts(Bank bank);
}
