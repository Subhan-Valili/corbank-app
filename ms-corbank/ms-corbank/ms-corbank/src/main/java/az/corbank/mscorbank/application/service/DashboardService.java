package az.corbank.mscorbank.application.service;

import az.corbank.mscorbank.domain.model.Account;
import az.corbank.mscorbank.domain.model.Bank;
import az.corbank.mscorbank.domain.model.BankConnection;
import az.corbank.mscorbank.domain.model.Dashboard;
import az.corbank.mscorbank.domain.model.Operation;
import az.corbank.mscorbank.domain.model.OperationFilter;
import az.corbank.mscorbank.application.port.in.GetDashboardUseCase;
import az.corbank.mscorbank.application.port.in.ListAccountsUseCase;
import az.corbank.mscorbank.application.port.in.ListOperationsUseCase;
import az.corbank.mscorbank.application.port.out.FxRatePort;
import az.corbank.mscorbank.application.port.out.OtherBanksPort;
import az.corbank.mscorbank.application.port.out.ProjectPort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

@Service
class DashboardService implements GetDashboardUseCase {

    private static final int RECENT_LIMIT = 5;

    private final ListAccountsUseCase accounts;
    private final ListOperationsUseCase operations;
    private final BankGateways banks;
    private final ProjectPort projects;
    private final OtherBanksPort otherBanks;
    private final FxRatePort fxRates;

    DashboardService(ListAccountsUseCase accounts, ListOperationsUseCase operations, BankGateways banks,
                     ProjectPort projects, OtherBanksPort otherBanks, FxRatePort fxRates) {
        this.accounts = accounts;
        this.operations = operations;
        this.banks = banks;
        this.projects = projects;
        this.otherBanks = otherBanks;
        this.fxRates = fxRates;
    }

    @Override
    public Dashboard getDashboard(Bank requested) {
        List<Account> allAccounts = accounts.listAccounts(null);

        // Only banks that really have accounts count as connected
        List<BankConnection> connected = Arrays.stream(Bank.values())
                .map(bank -> new BankConnection(bank, (int) allAccounts.stream().filter(a -> a.bank() == bank).count()))
                .filter(c -> c.accountCount() > 0)
                .toList();

        Bank selected = connected.stream().map(BankConnection::bank).anyMatch(b -> b == requested) ? requested : null;
        List<Account> visibleAccounts = selected == null
                ? allAccounts
                : allAccounts.stream().filter(a -> a.bank() == selected).toList();

        // statement of the first visible account ...
        List<Operation> firstAccountOperations = visibleAccounts.stream()
                .findFirst()
                .map(a -> operations.listOperations(a.id(), OperationFilter.none()).operations())
                .orElse(List.of())
                .stream()
                .limit(RECENT_LIMIT)
                .toList();

        // ... plus each (selected) bank's own cheap "recent history" view
        List<Operation> bankRecentOperations = banks.all().stream()
                .filter(g -> selected == null || g.bank() == selected)
                .flatMap(g -> banks.findRecentOperationsSafely(g, RECENT_LIMIT).stream())
                .toList();

        // The same operation can come from both sources — show it once
        Set<String> seen = new HashSet<>();
        List<Operation> recentOperations = Stream.concat(firstAccountOperations.stream(), bankRecentOperations.stream())
                .filter(op -> op.id() == null || seen.add(op.id() + "|" + op.currency() + "|" + op.direction()))
                .sorted(Comparator.comparing(Operation::date, Comparator.nullsLast(Comparator.<LocalDate>reverseOrder())))
                .limit(RECENT_LIMIT)
                .toList();

        return new Dashboard(
                visibleAccounts,
                projects.findAll(),
                recentOperations,
                otherBanks.findOtherBanks(),
                fxRates.findRates(),
                connected,
                selected);
    }
}
