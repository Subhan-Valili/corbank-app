package az.corbank.mscorbank.application.service;

import az.corbank.mscorbank.domain.model.Bank;
import az.corbank.mscorbank.domain.model.Operation;
import az.corbank.mscorbank.domain.model.OperationFilter;
import az.corbank.mscorbank.domain.model.OperationsReport;
import az.corbank.mscorbank.application.port.in.ListOperationsUseCase;
import az.corbank.mscorbank.application.port.out.BankGateway;
import az.corbank.mscorbank.application.port.out.BankUnavailableException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
class OperationService implements ListOperationsUseCase {

    private final BankGateways banks;

    OperationService(BankGateways banks) {
        this.banks = banks;
    }

    @Override
    public OperationsReport listOperations(String accountId, OperationFilter filter) {
        List<Operation> operations = fetchRaw(accountId, filter.fromDate(), filter.toDate()).stream()
                .filter(op -> op.matchesSearch(filter.search()))
                .filter(op -> op.matchesDirection(filter.direction()))
                .sorted(Comparator.comparing(Operation::date, Comparator.nullsLast(Comparator.<LocalDate>reverseOrder())))
                .toList();
        return OperationsReport.of(operations);
    }

    /**
     * An ABB account is served by ABB only (PASHA could answer with another account's operations).
     * Anything else is tried at PASHA first, then ABB. If every source fails the result is an empty
     * list, never an exception: the UI must show an empty table, not a 500.
     */
    private List<Operation> fetchRaw(String accountId, LocalDate from, LocalDate to) {
        boolean abbAccount = banks.findAccountSafely(Bank.ABB, accountId).isPresent();
        List<Bank> order = abbAccount ? List.of(Bank.ABB) : List.of(Bank.PASHA, Bank.ABB);

        for (Bank bank : order) {
            Optional<BankGateway> gateway = banks.get(bank);
            if (gateway.isEmpty()) continue;
            try {
                return gateway.get().findOperations(accountId, from, to);
            } catch (BankUnavailableException e) {
                log.debug("{} bu hesab üçün əməliyyat vermədi ({}): {}", bank, accountId, e.getMessage());
            }
        }
        return List.of();
    }
}
