package az.ingress.mspashabank.service.impl;

import az.ingress.mspashabank.dto.account.*;
import az.ingress.mspashabank.entity.AccountOperationEntity;
import az.ingress.mspashabank.entity.PashaAccountEntity;
import az.ingress.mspashabank.mapper.AccountOperationMapper;
import az.ingress.mspashabank.mapper.StatementOperationMapper;
import az.ingress.mspashabank.repository.AccountOperationRepository;
import az.ingress.mspashabank.repository.PashaBankAccountRepository;
import az.ingress.mspashabank.service.AccountOperationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static az.ingress.mspashabank.specification.AccountOperationSpecifications.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountOperationServiceImpl implements AccountOperationService {

    private static final int DEFAULT_PAGE_SIZE = 20;

    private static final Map<String, String> SORT_FIELD_MAP = Map.of(
            "date", "operationDate",
            "amount", "amountValue",
            "amountValue", "amountValue",
            "type", "type",
            "source", "source",
            "description", "description",
            "id", "externalId"
    );

    private final AccountOperationRepository accountOperationRepository;
    private final PashaBankAccountRepository pashaBankAccountRepository;
    private final AccountOperationMapper accountOperationMapper;
    private final StatementOperationMapper statementOperationMapper;

    @Override
    public AccountOperationsResponseDto getOperations(String accountId, LocalDate fromDate, LocalDate toDate) {
        log.info("Fetching account operations for accountId={}, fromDate={}, toDate={}", accountId, fromDate, toDate);

        PashaAccountEntity account = findAccountByIdOrAccountId(accountId);

        Specification<AccountOperationEntity> spec = hasAccountId(account.getId());

        if (fromDate != null) {
            spec = spec.and(operationDateFrom(fromDate.atStartOfDay()));
        }
        if (toDate != null) {
            spec = spec.and(operationDateTo(toDate.atTime(LocalTime.MAX)));
        }

        List<AccountOperationEntity> operations =
                accountOperationRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "operationDate"));

        return new AccountOperationsResponseDto(accountOperationMapper.toDtoList(operations));
    }

    @Override
    public AccountOperationsSearchResponseDto searchOperations(String accountId, OperationSearchRequestDto request) {
        log.info("Searching account operations for accountId={}, request={}", accountId, request);

        PashaAccountEntity account = findAccountByIdOrAccountId(accountId);

        Specification<AccountOperationEntity> spec = buildSpecification(account.getId(), request.filter());
        Sort sort = buildSort(request.sort());

        List<AccountOperationEntity> allMatched = accountOperationRepository.findAll(spec, sort);

        int total = allMatched.size();
        int offset = resolveOffset(request.pagination());
        int count = resolveCount(request.pagination());

        List<AccountOperationEntity> pageEntities = allMatched.stream()
                .skip(offset).limit(count).toList();
        List<AccountOperationDto> pageContent = accountOperationMapper.toDtoList(pageEntities);

        boolean hasNextPage = offset + pageContent.size() < total;
        return new AccountOperationsSearchResponseDto(pageContent,
                new AccountOperationsSearchResponseDto.PaginationDto(count, hasNextPage, offset, total));
    }

    @Override
    public CurrentStatementResponseDto getCurrentStatement(String accountId, CurrentStatementRequestDto request) {
        log.info("Fetching current statement for accountId={}, request={}", accountId, request);

        PashaAccountEntity account = findAccountByIdOrAccountId(accountId);

        Specification<AccountOperationEntity> spec = hasAccountId(account.getId());

        if (request != null) {
            if (request.fromDate() != null) {
                spec = spec.and(operationDateFrom(request.fromDate().atStartOfDay()));
            }
            if (request.toDate() != null) {
                spec = spec.and(operationDateTo(request.toDate().atTime(LocalTime.MAX)));
            }
        }

        int pageNumber = (request == null || request.pageNumber() == null || request.pageNumber() < 0)
                ? 0
                : request.pageNumber();
        int pageSize = DEFAULT_PAGE_SIZE;

        Pageable pageable = PageRequest.of(pageNumber, pageSize, Sort.by(Sort.Direction.DESC, "operationDate"));
        Page<AccountOperationEntity> pageResult = accountOperationRepository.findAll(spec, pageable);

        List<StatementOperationDto> operations = pageResult.getContent().stream()
                .map(op -> statementOperationMapper.toDto(op, account))
                .toList();

        CurrentStatementResponseDto.PaginationMetaDataDto paginationMetaData =
                CurrentStatementResponseDto.PaginationMetaDataDto.builder()
                        .currentPage(pageResult.getNumber())
                        .hasNextPage(pageResult.hasNext())
                        .hasPreviousPage(pageResult.hasPrevious())
                        .totalPages(pageResult.getTotalPages())
                        .build();

        return CurrentStatementResponseDto.builder()
                .availableClosingBalance(account.getAvailableBalance())
                .availableOpeningBalance(account.getTodayOpeningBalance())
                .closingBalance(account.getCurrentBalance())
                .closingBalanceAzn(account.getCurrentBalance())
                .message("Success")
                .openingBalance(account.getTodayOpeningBalance())
                .operations(operations)
                .paginationMetaData(paginationMetaData)
                .build();
    }

    @Override
    public DetailedStatementResponseDto getDetailedStatement(String iban, DetailedStatementRequestDto request) {
        log.info("Fetching detailed statement for iban={}, request={}", iban, request);

        PashaAccountEntity account = pashaBankAccountRepository.findAccountByIban(iban)
                .orElseThrow(() -> new RuntimeException("Account not found with IBAN: " + iban));

        Specification<AccountOperationEntity> spec = hasAccountId(account.getId());

        if (request != null) {
            if (request.fromDate() != null) {
                spec = spec.and(operationDateFrom(request.fromDate()));
            }
            if (request.toDate() != null) {
                spec = spec.and(operationDateTo(request.toDate()));
            }
            if (request.fromAmount() != null) {
                spec = spec.and(amountValueFrom(request.fromAmount()));
            }
            if (request.toAmount() != null) {
                spec = spec.and(amountValueTo(request.toAmount()));
            }
        }

        int page = 0;
        int size = DEFAULT_PAGE_SIZE;

        if (request != null && request.operationPaging() != null) {
            if (request.operationPaging().page() != null && request.operationPaging().page() >= 0) {
                page = request.operationPaging().page();
            }
            if (request.operationPaging().size() != null && request.operationPaging().size() > 0) {
                size = request.operationPaging().size();
            }
        }

        Sort sort = buildDetailedSort(request != null ? request.operationSort() : null);
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<AccountOperationEntity> pageResult = accountOperationRepository.findAll(spec, pageable);

        List<StatementOperationDto> content = pageResult.getContent().stream()
                .map(op -> statementOperationMapper.toDto(op, account))
                .toList();

        return DetailedStatementResponseDto.builder()
                .content(content)
                .pageNumber(pageResult.getNumber())
                .pageSize(pageResult.getSize())
                .totalElements(pageResult.getTotalElements())
                .totalPages(pageResult.getTotalPages())
                .build();
    }

    private PashaAccountEntity findAccountByIdOrAccountId(String accountId) {
        return pashaBankAccountRepository.findAccountByAccountId(accountId)
                .or(() -> {
                    try {
                        Long id = Long.parseLong(accountId);
                        return pashaBankAccountRepository.findById(id);
                    } catch (NumberFormatException e) {
                        return Optional.empty();
                    }
                })
                .orElseThrow(() -> new RuntimeException("Account not found with accountId: " + accountId));
    }

    private Sort buildDetailedSort(List<DetailedStatementRequestDto.SortDto> sortDtos) {
        if (sortDtos == null || sortDtos.isEmpty()) {
            return Sort.by(Sort.Direction.DESC, "operationDate");
        }

        Sort sort = Sort.unsorted();
        for (DetailedStatementRequestDto.SortDto s : sortDtos) {
            String field = "DATE".equalsIgnoreCase(s.field()) ? "operationDate" :
                    "AMOUNT".equalsIgnoreCase(s.field()) ? "amountValue" : "operationDate";
            Sort.Direction direction = "ASC".equalsIgnoreCase(s.direction()) ? Sort.Direction.ASC : Sort.Direction.DESC;
            sort = sort.and(Sort.by(direction, field));
        }
        return sort;
    }

    private Specification<AccountOperationEntity> buildSpecification(Long accountId, OperationSearchRequestDto.FilterDto filter) {
        Specification<AccountOperationEntity> spec = hasAccountId(accountId);

        if (filter == null) {
            return spec;
        }

        if (filter.date() != null) {
            if (StringUtils.hasText(filter.date().from())) {
                LocalDateTime from = LocalDate.parse(filter.date().from()).atStartOfDay();
                spec = spec.and(operationDateFrom(from));
            }
            if (StringUtils.hasText(filter.date().to())) {
                LocalDateTime to = LocalDate.parse(filter.date().to()).atTime(LocalTime.MAX);
                spec = spec.and(operationDateTo(to));
            }
        }

        if (filter.amountValue() != null) {
            if (StringUtils.hasText(filter.amountValue().from())) {
                spec = spec.and(amountValueFrom(new BigDecimal(filter.amountValue().from())));
            }
            if (StringUtils.hasText(filter.amountValue().to())) {
                spec = spec.and(amountValueTo(new BigDecimal(filter.amountValue().to())));
            }
        }

        if (StringUtils.hasText(filter.searchValue())) {
            spec = spec.and(searchValueLike(filter.searchValue()));
        }

        if (StringUtils.hasText(filter.activation())) {
            log.warn("'activation' filter dəyəri hazırda dəstəklənmir, ignore olunur: {}", filter.activation());
        }

        return spec;
    }

    private Sort buildSort(List<OperationSearchRequestDto.SortRequestDto> sortRequest) {
        if (sortRequest == null || sortRequest.isEmpty()) {
            return Sort.by(Sort.Direction.DESC, "operationDate");
        }

        Sort sort = Sort.unsorted();
        for (OperationSearchRequestDto.SortRequestDto s : sortRequest) {
            String field = SORT_FIELD_MAP.getOrDefault(s.orderBy(), "operationDate");
            Sort.Direction direction = "ASC".equalsIgnoreCase(s.order()) ? Sort.Direction.ASC : Sort.Direction.DESC;
            sort = sort.and(Sort.by(direction, field));
        }
        return sort;
    }

    private int resolveOffset(OperationSearchRequestDto.PaginationDto pagination) {
        if (pagination == null || pagination.offset() == null || pagination.offset() < 0) {
            return 0;
        }
        return pagination.offset();
    }

    private int resolveCount(OperationSearchRequestDto.PaginationDto pagination) {
        if (pagination == null || pagination.count() == null || pagination.count() <= 0) {
            return DEFAULT_PAGE_SIZE;
        }
        return pagination.count();
    }
}