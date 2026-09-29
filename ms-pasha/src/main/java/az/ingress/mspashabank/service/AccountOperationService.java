package az.ingress.mspashabank.service;

import az.ingress.mspashabank.dto.account.*;

import java.time.LocalDate;

public interface AccountOperationService {

    AccountOperationsResponseDto getOperations(String accountId, LocalDate fromDate, LocalDate toDate);

    AccountOperationsSearchResponseDto searchOperations(String accountId, OperationSearchRequestDto request);

    CurrentStatementResponseDto getCurrentStatement(String accountId, CurrentStatementRequestDto request);

    DetailedStatementResponseDto getDetailedStatement(String iban, DetailedStatementRequestDto request);
}
