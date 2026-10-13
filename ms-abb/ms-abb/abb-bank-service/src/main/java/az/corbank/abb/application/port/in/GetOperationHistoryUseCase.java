package az.corbank.abb.application.port.in;

import az.corbank.abb.domain.model.CustomerIdentity;
import az.corbank.abb.domain.model.StatementLine;

import java.util.List;

/**
 * Reads previously-persisted operations from our own database — no live call to ABB.
 * Backs cases where we want "who paid how much and when" cheaply and repeatedly (e.g. the
 * dashboard's "Son əməliyyatlar" on every page load) without hitting ABB's statement
 * endpoint every time. Data here is only as fresh as the last GetAccountStatementUseCase
 * call that populated it (see AccountApplicationService.getStatement()).
 */
public interface GetOperationHistoryUseCase {

    /** @throws az.corbank.abb.domain.exception.AccountNotFoundException if the account isn't this customer's */
    List<StatementLine> getHistoryForAccount(CustomerIdentity identity, String accountNumber, int limit);

    /** Recent lines across all of THIS customer's accounts (never other customers'). */
    List<StatementLine> getRecentHistory(CustomerIdentity identity, int limit);
}
