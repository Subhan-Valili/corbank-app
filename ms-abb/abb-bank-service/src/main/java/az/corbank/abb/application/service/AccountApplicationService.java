package az.corbank.abb.application.service;

import az.corbank.abb.application.port.in.GetAccountBalanceUseCase;
import az.corbank.abb.application.port.in.GetAccountStatementUseCase;
import az.corbank.abb.application.port.in.ListAccountsUseCase;
import az.corbank.abb.application.port.out.AbbBankGateway;
import az.corbank.abb.application.port.out.AccountSnapshotRepositoryPort;
import az.corbank.abb.domain.exception.AbbGatewayException;
import az.corbank.abb.domain.model.AccountBalance;
import az.corbank.abb.domain.model.AccountStatement;
import az.corbank.abb.domain.model.CorporateAccount;
import az.corbank.abb.domain.model.DataOrigin;
import az.corbank.abb.domain.model.StatementQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Orchestrates the AbbBankGateway (out port) and AccountSnapshotRepositoryPort (out port)
 * to serve the three account-related use cases. The one real piece of business logic here
 * is the balance fallback: if the gateway fails, fall back to the last persisted snapshot
 * rather than failing outright — see getBalance().
 */
public class AccountApplicationService implements ListAccountsUseCase, GetAccountBalanceUseCase, GetAccountStatementUseCase {

    private static final Logger log = LoggerFactory.getLogger(AccountApplicationService.class);

    private final AbbBankGateway gateway;
    private final AccountSnapshotRepositoryPort snapshotRepository;

    public AccountApplicationService(AbbBankGateway gateway, AccountSnapshotRepositoryPort snapshotRepository) {
        this.gateway = gateway;
        this.snapshotRepository = snapshotRepository;
    }

    @Override
    public List<CorporateAccount> listAccounts() {
        return gateway.listAccounts();
    }

    @Override
    public AccountBalance getBalance(String accountNumber) {
        try {
            AccountBalance live = gateway.getBalance(accountNumber);
            snapshotRepository.save(live);
            return live;
        } catch (AbbGatewayException e) {
            return snapshotRepository.findByAccountNumber(accountNumber)
                    .map(snapshot -> {
                        log.warn("ABB balance call failed for {}, serving last known snapshot from {}",
                                accountNumber, snapshot.fetchedAt());
                        return new AccountBalance(snapshot.accountNumber(), snapshot.currency(),
                                snapshot.availableBalance(), snapshot.fetchedAt(), DataOrigin.SNAPSHOT_FALLBACK);
                    })
                    .orElseThrow(() -> e);
        }
    }

    @Override
    public AccountStatement getStatement(StatementQuery query) {
        return gateway.getStatement(query);
    }
}
