package az.corbank.abb.application.port.out;

import az.corbank.abb.domain.model.StatementLine;

import java.util.List;

/**
 * Persists the operation/payment history we've fetched from ABB, so "who paid how much,
 * to/from whom, and when" is answerable straight from our own database — without calling
 * ABB live every time (e.g. every dashboard render) and without losing history that falls
 * outside whatever date range a given statement fetch happened to cover.
 */
public interface OperationHistoryRepositoryPort {

    /**
     * Upserts every line for this account (matched by reference, so re-fetching an
     * overlapping date range doesn't create duplicates).
     */
    void saveAll(String accountNumber, List<StatementLine> lines);

    /** Most recent lines for one account, newest first. */
    List<StatementLine> findRecentByAccountNumber(String accountNumber, int limit);

    /** Most recent lines across ALL accounts, newest first — backs the dashboard's "Son əməliyyatlar". */
    List<StatementLine> findRecentAcrossAllAccounts(int limit);
}
