package az.corbank.abb.adapter.out.persistence;

import az.corbank.abb.application.port.out.OperationHistoryRepositoryPort;
import az.corbank.abb.domain.model.Direction;
import az.corbank.abb.domain.model.StatementLine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@Slf4j
@Component
class OperationHistoryPersistenceAdapter implements OperationHistoryRepositoryPort {

    private static final DateTimeFormatter ABB_DATE = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private final OperationHistoryJpaRepository jpaRepository;

    OperationHistoryPersistenceAdapter(OperationHistoryJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    @Transactional
    public void saveAll(String accountNumber, List<StatementLine> lines) {
        for (StatementLine line : lines) {
            OperationHistoryJpaEntity entity = jpaRepository
                    .findByAccountNumberAndReference(accountNumber, line.reference())
                    .orElseGet(OperationHistoryJpaEntity::new);

            entity.setAccountNumber(accountNumber);
            entity.setReference(line.reference());
            entity.setTransactionDateRaw(line.date());
            entity.setTransactionDate(parseDate(line.date()));
            entity.setDescription(line.description());
            entity.setCounterparty(line.counterparty());
            entity.setBeneficiaryTin(line.beneficiaryTin());
            entity.setAmount(line.amount());
            entity.setCurrency(line.currency());
            entity.setDirection(line.direction().name());
            entity.setFetchedAt(Instant.now());

            jpaRepository.save(entity);
        }
    }

    @Override
    public List<StatementLine> findRecentByAccountNumber(String accountNumber, int limit) {
        Pageable page = PageRequest.of(0, limit);
        return jpaRepository.findByAccountNumberOrderByTransactionDateDescIdDesc(accountNumber, page).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<StatementLine> findRecentAcrossAllAccounts(int limit) {
        Pageable page = PageRequest.of(0, limit);
        return jpaRepository.findAllByOrderByTransactionDateDescIdDesc(page).stream()
                .map(this::toDomain)
                .toList();
    }

    private StatementLine toDomain(OperationHistoryJpaEntity e) {
        return new StatementLine(
                e.getAccountNumber(), e.getReference(), e.getTransactionDateRaw(), e.getDescription(),
                e.getCounterparty(), e.getBeneficiaryTin(), e.getAmount(), e.getCurrency(),
                Direction.valueOf(e.getDirection()));
    }

    private LocalDate parseDate(String raw) {
        if (raw == null || raw.isBlank()) return null;
        try {
            return LocalDate.parse(raw, ABB_DATE);
        } catch (DateTimeParseException e) {
            log.warn("Could not parse ABB transaction date '{}', history row will sort last", raw);
            return null;
        }
    }
}
