package az.corbank.abb.application.service;

import az.corbank.abb.application.port.in.GetAccountStatementUseCase;
import az.corbank.abb.application.port.in.GetOperationHistoryUseCase;
import az.corbank.abb.application.port.in.ListAccountsUseCase;
import az.corbank.abb.application.port.out.AbbBankGateway;
import az.corbank.abb.application.port.out.OperationHistoryRepositoryPort;
import az.corbank.abb.domain.exception.AccountNotFoundException;
import az.corbank.abb.domain.model.AccountStatement;
import az.corbank.abb.domain.model.CorporateAccount;
import az.corbank.abb.domain.model.CustomerIdentity;
import az.corbank.abb.domain.model.StatementLine;
import az.corbank.abb.domain.model.StatementQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Orchestrates the AbbBankGateway (out port) and OperationHistoryRepositoryPort (out port)
 * for the dashboard's account-related use cases.
 *
 * Every use case is scoped to a CustomerIdentity (PIN and/or mobile number sent by
 * ms-corbank): a customer only ever sees their own accounts, and asking for the statement
 * or history of an account that isn't theirs fails with AccountNotFoundException.
 *
 * getStatement() does two things: fetches the live statement from ABB (the "source of
 * truth" call), and — as a side effect — persists every line into operation history, so
 * "who paid how much and when" accumulates in our own database over time rather than only
 * ever existing inside whatever date range the last live call happened to cover. That
 * write is best-effort: if persistence fails, the caller still gets their live statement;
 * we just log it rather than fail the whole request over a history-write problem.
 *
 * getHistoryForAccount()/getRecentHistory() are pure DB reads — no ABB call — for callers
 * (like the dashboard) that want "recent operations" cheaply and repeatedly without
 * hitting ABB's statement endpoint on every page load.
 */
public class AccountApplicationService implements ListAccountsUseCase, GetAccountStatementUseCase, GetOperationHistoryUseCase {

    private static final Logger log = LoggerFactory.getLogger(AccountApplicationService.class);

    private static final int MAX_HISTORY_LIMIT = 200;

    private final AbbBankGateway gateway;
    private final OperationHistoryRepositoryPort historyRepository;

    public AccountApplicationService(AbbBankGateway gateway, OperationHistoryRepositoryPort historyRepository) {
        this.gateway = gateway;
        this.historyRepository = historyRepository;
    }

    @Override
    public List<CorporateAccount> listAccounts(CustomerIdentity identity) {
        return gateway.listAccounts(identity);
    }

    @Override
    public AccountStatement getStatement(CustomerIdentity identity, StatementQuery query) {
        CorporateAccount owned = requireOwnedAccount(identity, query.accountNumber());
        // Always continue with the canonical account number (the caller may have passed the IBAN),
        // so history rows are stored under one consistent key.
        StatementQuery resolved = new StatementQuery(owned.accountNumber(), query.fromDate(), query.toDate(),
                query.page(), query.pageSize(), query.operationType());
        AccountStatement statement = gateway.getStatement(resolved);
        persistHistory(owned.accountNumber(), statement.lines());
        return statement;
    }

    @Override
    public List<StatementLine> getHistoryForAccount(CustomerIdentity identity, String accountNumber, int limit) {
        CorporateAccount owned = requireOwnedAccount(identity, accountNumber);
        return historyRepository.findRecentByAccountNumber(owned.accountNumber(), clamp(limit));
    }

    @Override
    public List<StatementLine> getRecentHistory(CustomerIdentity identity, int limit) {
        List<String> accountNumbers = gateway.listAccounts(identity).stream()
                .map(CorporateAccount::accountNumber)
                .toList();
        return historyRepository.findRecentByAccountNumbers(accountNumbers, clamp(limit));
    }

    private CorporateAccount requireOwnedAccount(CustomerIdentity identity, String accountNumber) {
        if (accountNumber == null) {
            throw new AccountNotFoundException("Account not found.");
        }
        return gateway.listAccounts(identity).stream()
                .filter(a -> accountNumber.equalsIgnoreCase(a.accountNumber())
                        || accountNumber.equalsIgnoreCase(a.iban()))
                .findFirst()
                .orElseThrow(() -> new AccountNotFoundException("Account not found: " + accountNumber));
    }

    private int clamp(int limit) {
        return Math.max(1, Math.min(limit, MAX_HISTORY_LIMIT));
    }

    private void persistHistory(String accountNumber, List<StatementLine> lines) {
        try {
            historyRepository.saveAll(accountNumber, lines);
        } catch (RuntimeException e) {
            log.warn("Failed to persist operation history for {}: {}", accountNumber, e.getMessage());
        }
    }
}
