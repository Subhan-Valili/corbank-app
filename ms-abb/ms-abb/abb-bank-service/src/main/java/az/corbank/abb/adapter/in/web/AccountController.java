package az.corbank.abb.adapter.in.web;

import az.corbank.abb.adapter.in.web.dto.AccountStatementResponse;
import az.corbank.abb.adapter.in.web.dto.CorporateAccountResponse;
import az.corbank.abb.adapter.in.web.dto.OperationLineResponse;
import az.corbank.abb.application.port.in.GetAccountStatementUseCase;
import az.corbank.abb.application.port.in.GetOperationHistoryUseCase;
import az.corbank.abb.application.port.in.ListAccountsUseCase;
import az.corbank.abb.domain.model.CustomerIdentity;
import az.corbank.abb.domain.model.StatementQuery;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Inbound (driving) adapter: translates HTTP <-> use case calls. Depends only on the
 * input ports (interfaces) — never on the application services directly, and never on
 * anything in the outbound adapters.
 */
@RestController
@RequestMapping("/internal/abb/accounts")
class AccountController {

    private final ListAccountsUseCase listAccounts;
    private final GetAccountStatementUseCase getStatement;
    private final GetOperationHistoryUseCase getHistory;

    AccountController(ListAccountsUseCase listAccounts, GetAccountStatementUseCase getStatement,
                       GetOperationHistoryUseCase getHistory) {
        this.listAccounts = listAccounts;
        this.getStatement = getStatement;
        this.getHistory = getHistory;
    }

    /**
     * Who ms-corbank is calling on behalf of. Sent as headers (not query params) so the PIN
     * never ends up in access logs / proxy logs. Individuals: X-Customer-Pin. Corporate
     * customers have no PIN: X-Customer-Mobile. At least one is required.
     */
    private static CustomerIdentity identity(String pin, String mobile) {
        return new CustomerIdentity(pin, mobile);
    }

    /** GET /internal/abb/accounts — spec §4.21, the "list my accounts" source (this customer's only). */
    @GetMapping
    List<CorporateAccountResponse> listAccounts(
            @RequestHeader(value = "X-Customer-Pin", required = false) String pin,
            @RequestHeader(value = "X-Customer-Mobile", required = false) String mobile) {
        return listAccounts.listAccounts(identity(pin, mobile)).stream().map(CorporateAccountResponse::from).toList();
    }

    /**
     * GET /internal/abb/accounts/{accountNumber}/statement?fromDate=YYYYMMDD&toDate=YYYYMMDD&page=&pageSize=&operationType=
     * LIVE call to ABB (spec §4.10). Every line returned here is also persisted into
     * operation history as a side effect — see AccountApplicationService.getStatement().
     */
    @GetMapping("/{accountNumber}/statement")
    AccountStatementResponse getStatement(
            @RequestHeader(value = "X-Customer-Pin", required = false) String pin,
            @RequestHeader(value = "X-Customer-Mobile", required = false) String mobile,
            @PathVariable String accountNumber,
            @RequestParam String fromDate,
            @RequestParam String toDate,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String operationType) {

        StatementQuery.OperationType type = operationType == null ? StatementQuery.OperationType.ALL
                : StatementQuery.OperationType.valueOf(operationType.toUpperCase());

        StatementQuery query = new StatementQuery(accountNumber, fromDate, toDate, page, pageSize, type);
        return AccountStatementResponse.from(getStatement.getStatement(identity(pin, mobile), query));
    }

    /**
     * GET /internal/abb/accounts/{accountNumber}/history?limit=
     * Pure DB read — no ABB call. Cheap to call repeatedly (e.g. on every dashboard load).
     * Only reflects what a prior /statement call has already persisted for this account.
     */
    @GetMapping("/{accountNumber}/history")
    List<OperationLineResponse> getHistory(
            @RequestHeader(value = "X-Customer-Pin", required = false) String pin,
            @RequestHeader(value = "X-Customer-Mobile", required = false) String mobile,
            @PathVariable String accountNumber,
            @RequestParam(defaultValue = "20") int limit) {
        return getHistory.getHistoryForAccount(identity(pin, mobile), accountNumber, limit).stream()
                .map(OperationLineResponse::from)
                .toList();
    }

    /**
     * GET /internal/abb/accounts/history/recent?limit=
     * Pure DB read across all of THIS customer's accounts — backs the dashboard's "Son
     * əməliyyatlar" without needing to know which account to ask first.
     */
    @GetMapping("/history/recent")
    List<OperationLineResponse> getRecentHistory(
            @RequestHeader(value = "X-Customer-Pin", required = false) String pin,
            @RequestHeader(value = "X-Customer-Mobile", required = false) String mobile,
            @RequestParam(defaultValue = "20") int limit) {
        return getHistory.getRecentHistory(identity(pin, mobile), limit).stream()
                .map(OperationLineResponse::from)
                .toList();
    }
}
