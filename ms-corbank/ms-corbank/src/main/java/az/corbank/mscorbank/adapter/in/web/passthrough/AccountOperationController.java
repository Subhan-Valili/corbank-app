package az.corbank.mscorbank.adapter.in.web.passthrough;

import az.corbank.mscorbank.adapter.out.pasha.dto.account.*;
import az.corbank.mscorbank.adapter.out.pasha.client.AccountOperationFeignClient;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/accounts/{accountId}")
@RequiredArgsConstructor
public class AccountOperationController {

    private final AccountOperationFeignClient accountOperationFeignClient;

    @GetMapping("/operations")
    public ResponseEntity<AccountOperationsResponseDto> getOperations(
            @PathVariable("accountId") String accountId,
            @RequestParam(value = "fromDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(value = "toDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        return ResponseEntity.ok(accountOperationFeignClient.getOperations(accountId, fromDate, toDate));
    }

    @PostMapping("/operations")
    public ResponseEntity<AccountOperationsSearchResponseDto> searchOperations(
            @PathVariable("accountId") String accountId,
            @RequestBody OperationSearchRequestDto request) {
        return ResponseEntity.ok(accountOperationFeignClient.searchOperations(accountId, request));
    }

    @PostMapping("/statements/current")
    public ResponseEntity<CurrentStatementResponseDto> getCurrentStatement(
            @PathVariable("accountId") String accountId,
            @RequestBody CurrentStatementRequestDto request) {
        return ResponseEntity.ok(accountOperationFeignClient.getCurrentStatement(accountId, request));
    }

    @PostMapping("/statements/detailed")
    public ResponseEntity<DetailedStatementResponseDto> getDetailedStatement(
            @PathVariable("accountId") String iban,
            @RequestBody DetailedStatementRequestDto request) {
        return ResponseEntity.ok(accountOperationFeignClient.getDetailedStatement(iban, request));
    }
}
