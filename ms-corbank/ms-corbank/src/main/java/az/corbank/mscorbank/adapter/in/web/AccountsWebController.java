package az.corbank.mscorbank.adapter.in.web;

import az.corbank.mscorbank.domain.model.Account;
import az.corbank.mscorbank.domain.model.OperationFilter;
import az.corbank.mscorbank.adapter.in.web.dto.*;
import az.corbank.mscorbank.application.port.in.GetAccountUseCase;
import az.corbank.mscorbank.application.port.in.ListAccountsUseCase;
import az.corbank.mscorbank.application.port.in.ListOperationsUseCase;
import az.corbank.mscorbank.application.port.in.MakePaymentUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Optional;


@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AccountsWebController {

    private final ListAccountsUseCase listAccounts;
    private final GetAccountUseCase getAccount;
    private final ListOperationsUseCase listOperations;
    private final MakePaymentUseCase makePayment;

    @GetMapping("/accounts")
    public ResponseEntity<AccountsResponse> getAccounts(@RequestParam(value = "bank", required = false) String bank) {
        var accounts = listAccounts.listAccounts(WebMapper.parseBank(bank)).stream().map(WebMapper::toDto).toList();
        return ResponseEntity.ok(new AccountsResponse(accounts));
    }

    @GetMapping("/accounts/{id}")
    public ResponseEntity<AccountResponse> getAccount(@PathVariable("id") String id) {
        Optional<Account> account = getAccount.getAccount(id);
        return account.map(a -> ResponseEntity.ok(new AccountResponse(WebMapper.toDto(a))))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/accounts/{id}/operations")
    public ResponseEntity<OperationsResponse> getOperations(
            @PathVariable("id") String id,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "direction", required = false) String direction) {
        OperationFilter filter = new OperationFilter(startDate, endDate, search, parseDirection(direction));
        return ResponseEntity.ok(WebMapper.toDto(listOperations.listOperations(id, filter)));
    }

    @PostMapping("/payments")
    public ResponseEntity<PaymentResultDto> pay(@RequestBody PaymentRequestDto request) {
        return ResponseEntity.ok(WebMapper.toDto(makePayment.pay(WebMapper.toCommand(request))));
    }

    /** frontend "Bütün əməliyyatlar" üçün direction=all göndərir — bu "süzgəc yoxdur" deməkdir. */
    private az.corbank.mscorbank.domain.model.OperationDirection parseDirection(String direction) {
        if (direction == null || direction.isBlank() || "all".equalsIgnoreCase(direction)) return null;
        return "in".equalsIgnoreCase(direction) ? az.corbank.mscorbank.domain.model.OperationDirection.IN : az.corbank.mscorbank.domain.model.OperationDirection.OUT;
    }
}
