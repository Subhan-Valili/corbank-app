package az.corbank.abb.application.service;

import az.corbank.abb.application.port.in.GetAccountStatementUseCase;
import az.corbank.abb.application.port.in.GetOperationHistoryUseCase;
import az.corbank.abb.application.port.in.ListAccountsUseCase;
import az.corbank.abb.application.port.out.AbbBankGateway;
import az.corbank.abb.application.port.out.OperationHistoryRepositoryPort;
import az.corbank.abb.domain.model.AccountStatement;
import az.corbank.abb.domain.model.CorporateAccount;
import az.corbank.abb.domain.model.StatementLine;
import az.corbank.abb.domain.model.StatementQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Orchestrates the AbbBankGateway (out port) and OperationHistoryRepositoryPort (out port)
 * for the dashboard's account-related use cases.
 *
 * getStatement() does two things: fetches the live statement from ABB (the "source of
 * truth" call), and — as a side effect — persists every line into operation history, so
 * "who paid how much and when" accumulates in our own database over time rather than only
 * ever existing inside whatever date range the last live call happened to cover. That
 * write is best-effort: if persistence fails, the caller still gets their live statement;
 * we just log it rather than fail the whole request over a history-write problem.
 *
 * getHistoryForAccount()/getRecentHistoryAcrossAllAccounts() are pure DB reads — no ABB
 * call — for callers (like the dashboard) that want "recent operations" cheaply and
 * repeatedly without hitting ABB's statement endpoint on every page load.
 */
public class AccountApplicationService implements ListAccountsUseCase, GetAccountStatementUseCase, GetOperationHistoryUseCase {

    private static final Logger log = LoggerFactory.getLogger(AccountApplicationService.class);

    private final AbbBankGateway gateway;
    private final OperationHistoryRepositoryPort historyRepository;

    public AccountApplicationService(AbbBankGateway gateway, OperationHistoryRepositoryPort historyRepository) {
        this.gateway = gateway;
        this.historyRepository = historyRepository;
    }

    @Override
    public List<CorporateAccount> listAccounts() {
        return gateway.listAccounts();
    }

    @Override
    public AccountStatement getStatement(StatementQuery query) {
        AccountStatement statement = gateway.getStatement(query);
        persistHistory(query.accountNumber(), statement.lines());
        return statement;
    }

    @Override
    public List<StatementLine> getHistoryForAccount(String accountNumber, int limit) {
        return historyRepository.findRecentByAccountNumber(accountNumber, limit);
    }

    @Override
    public List<StatementLine> getRecentHistoryAcrossAllAccounts(int limit) {
        return historyRepository.findRecentAcrossAllAccounts(limit);
    }

    private void persistHistory(String accountNumber, List<StatementLine> lines) {
        try {
            historyRepository.saveAll(accountNumber, lines);
        } catch (RuntimeException e) {
            log.warn("Failed to persist operation history for {}: {}", accountNumber, e.getMessage());
        }
    }
}
