package az.corbank.mscorbank.domain.model;

import java.util.List;

public record Dashboard(
        List<Account> accounts,
        List<Project> projects,
        List<Operation> recentOperations,
        List<BankBalance> otherBanks,
        List<FxRate> exchangeRates,
        List<BankConnection> connectedBanks,
        Bank selectedBank            // null = all banks
) {
}
