package az.corbank.abb.application.port.in;

import az.corbank.abb.domain.model.CorporateAccount;

import java.util.List;

public interface ListAccountsUseCase {
    List<CorporateAccount> listAccounts();
}
