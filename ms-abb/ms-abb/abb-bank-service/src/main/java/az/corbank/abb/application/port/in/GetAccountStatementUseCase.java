package az.corbank.abb.application.port.in;

import az.corbank.abb.domain.model.AccountStatement;
import az.corbank.abb.domain.model.CustomerIdentity;
import az.corbank.abb.domain.model.StatementQuery;

public interface GetAccountStatementUseCase {
    /** @throws az.corbank.abb.domain.exception.AccountNotFoundException if the account isn't this customer's */
    AccountStatement getStatement(CustomerIdentity identity, StatementQuery query);
}
