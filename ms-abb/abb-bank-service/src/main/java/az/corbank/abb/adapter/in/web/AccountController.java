package az.corbank.abb.adapter.in.web;

import az.corbank.abb.adapter.in.web.dto.AccountBalanceResponse;
import az.corbank.abb.adapter.in.web.dto.AccountStatementResponse;
import az.corbank.abb.adapter.in.web.dto.CorporateAccountResponse;
import az.corbank.abb.application.port.in.GetAccountBalanceUseCase;
import az.corbank.abb.application.port.in.GetAccountStatementUseCase;
import az.corbank.abb.application.port.in.ListAccountsUseCase;
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
    private final GetAccountBalanceUseCase getBalance;
    private final GetAccountStatementUseCase getStatement;

    AccountController(ListAccountsUseCase listAccounts, GetAccountBalanceUseCase getBalance,
                       GetAccountStatementUseCase getStatement) {
        this.listAccounts = listAccounts;
        this.getBalance = getBalance;
        this.getStatement = getStatement;
    }

    /** GET /internal/abb/accounts — spec §4.21, the "list my accounts" source. */
    @GetMapping
    List<CorporateAccountResponse> listAccounts() {
        return listAccounts.listAccounts().stream().map(CorporateAccountResponse::from).toList();
    }

    /** GET /internal/abb/accounts/{accountNumber}/balance */
    @GetMapping("/{accountNumber}/balance")
    AccountBalanceResponse getBalance(@PathVariable String accountNumber) {
        return AccountBalanceResponse.from(getBalance.getBalance(accountNumber));
    }

    /** GET /internal/abb/accounts/{accountNumber}/statement?fromDate=YYYYMMDD&toDate=YYYYMMDD&page=&pageSize=&operationType= */
    @GetMapping("/{accountNumber}/statement")
    AccountStatementResponse getStatement(
            @PathVariable String accountNumber,
            @RequestParam String fromDate,
            @RequestParam String toDate,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String operationType) {

        StatementQuery.OperationType type = operationType == null ? StatementQuery.OperationType.ALL
                : StatementQuery.OperationType.valueOf(operationType.toUpperCase());

        StatementQuery query = new StatementQuery(accountNumber, fromDate, toDate, page, pageSize, type);
        return AccountStatementResponse.from(getStatement.getStatement(query));
    }
}
