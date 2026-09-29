package az.ingress.mspashabank.controller;

import az.ingress.mspashabank.dto.account.*;
import az.ingress.mspashabank.service.AccountOperationService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
public class AccountOperationController {

    private final AccountOperationService accountOperationService;

    @GetMapping("/{accountId}/operations")
    public ResponseEntity<AccountOperationsResponseDto> getOperations(
            @PathVariable String accountId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        return ResponseEntity.ok(accountOperationService.getOperations(accountId, fromDate, toDate));
    }

    @PostMapping("/{accountId}/operations")
    public ResponseEntity<AccountOperationsSearchResponseDto> searchOperations(
            @PathVariable String accountId,
            @RequestBody OperationSearchRequestDto request) {
        return ResponseEntity.ok(accountOperationService.searchOperations(accountId, request));
    }

    @PostMapping("/{accountId}/statements/current")
    public ResponseEntity<CurrentStatementResponseDto> getCurrentStatement(
            @PathVariable String accountId,
            @RequestBody CurrentStatementRequestDto request) {
        return ResponseEntity.ok(accountOperationService.getCurrentStatement(accountId, request));
    }

    @PostMapping("/{iban}/statements/detailed")
    public ResponseEntity<DetailedStatementResponseDto> getDetailedStatement(
            @PathVariable String iban,
            @RequestBody DetailedStatementRequestDto request) {
        return ResponseEntity.ok(accountOperationService.getDetailedStatement(iban, request));
    }
}