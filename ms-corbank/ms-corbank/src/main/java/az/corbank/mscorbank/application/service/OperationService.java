package az.corbank.mscorbank.application.service;

import az.corbank.mscorbank.application.exception.AccountNotFoundException;
import az.corbank.mscorbank.domain.model.Account;
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
     * Hesab əvvəlcə cari istifadəçinin öz hesabları arasında tapılır (başqasının hesabı -> 404),
     * sonra əməliyyatlar məhz həmin hesabın bankından alınır. Bank cavab verməsə nəticə boş siyahıdır,
     * exception yox: UI 500 yox, boş cədvəl göstərməlidir.
     */
    private List<Operation> fetchRaw(String accountId, LocalDate from, LocalDate to) {
        Account account = banks.findOwnedAccount(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));
        BankGateway gateway = banks.require(account.bank());
        try {
            return gateway.findOperations(account.id(), from, to);
        } catch (BankUnavailableException e) {
            log.debug("{} bu hesab üçün əməliyyat vermədi ({}): {}", account.bank(), accountId, e.getMessage());
            return List.of();
        }
    }
}
