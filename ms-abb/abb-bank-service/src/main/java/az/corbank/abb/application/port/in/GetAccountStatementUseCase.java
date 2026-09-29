package az.corbank.abb.application.port.in;

import az.corbank.abb.domain.model.AccountStatement;
import az.corbank.abb.domain.model.StatementQuery;

public interface GetAccountStatementUseCase {
    AccountStatement getStatement(StatementQuery query);
}
