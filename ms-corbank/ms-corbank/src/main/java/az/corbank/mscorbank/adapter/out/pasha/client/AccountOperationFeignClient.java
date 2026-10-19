package az.corbank.mscorbank.adapter.out.pasha.client;

import az.corbank.mscorbank.adapter.out.pasha.dto.account.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

/**
 * ms-pasha (AccountOperationController) üçün Feign client.
 * Base URL "ms-pasha.base-url" property-si ilə verilir (bax: application.yml).
 */
@FeignClient(name = "ms-pasha-operation-client", url = "${ms-pasha.base-url}")
public interface AccountOperationFeignClient {

    @GetMapping("/api/v1/accounts/{accountId}/operations")
    AccountOperationsResponseDto getOperations(
            @PathVariable("accountId") String accountId,
            @RequestParam(value = "fromDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(value = "toDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate);

    @PostMapping("/api/v1/accounts/{accountId}/operations")
    AccountOperationsSearchResponseDto searchOperations(
            @PathVariable("accountId") String accountId,
            @RequestBody OperationSearchRequestDto request);

    @PostMapping("/api/v1/accounts/{accountId}/statements/current")
    CurrentStatementResponseDto getCurrentStatement(
            @PathVariable("accountId") String accountId,
            @RequestBody CurrentStatementRequestDto request);

    @PostMapping("/api/v1/accounts/{iban}/statements/detailed")
    DetailedStatementResponseDto getDetailedStatement(
            @PathVariable("iban") String iban,
            @RequestBody DetailedStatementRequestDto request);
}

