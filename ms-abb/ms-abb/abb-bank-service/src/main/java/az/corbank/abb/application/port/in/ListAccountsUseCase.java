package az.corbank.abb.application.port.in;

import az.corbank.abb.domain.model.CorporateAccount;
import az.corbank.abb.domain.model.CustomerIdentity;

import java.util.List;

public interface ListAccountsUseCase {
    /** Only the accounts belonging to this customer. */
    List<CorporateAccount> listAccounts(CustomerIdentity identity);
}
