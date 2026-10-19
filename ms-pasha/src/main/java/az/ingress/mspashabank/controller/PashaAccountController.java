package az.ingress.mspashabank.controller;

import az.ingress.mspashabank.dto.account.PashaAccountResponseDto;
import az.ingress.mspashabank.service.PashaBankAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
public class PashaAccountController {
    private final PashaBankAccountService  pashaBankAccountService;

    @GetMapping
    public List<PashaAccountResponseDto> findAllAccountsByCustomerNo(@RequestParam String customerNo) {
        return pashaBankAccountService.getAccounts(customerNo);
    }

    @GetMapping("/by-pin")
    public List<PashaAccountResponseDto> findAllAccountsByPin(@RequestHeader("X-Pin") String pin) {
        return pashaBankAccountService.getAccountsByPin(pin);
    }

    @GetMapping("/by-tin")
    public List<PashaAccountResponseDto> findAllAccountsByTin(@RequestParam String tin) {
        return pashaBankAccountService.getAccountsByTin(tin);
    }

    @GetMapping("/{accountId}")
    public PashaAccountResponseDto findAccountByAccountId(@PathVariable String accountId) {
        return pashaBankAccountService.getAccountByAccountId(accountId);
    }

    @GetMapping("/iban/{iban}")
    public PashaAccountResponseDto findAccountByIban(@PathVariable String iban) {
        return pashaBankAccountService.getAccountByIban(iban);
    }

    @GetMapping("/byKey")
    public List<PashaAccountResponseDto> getAccounts(
            @RequestHeader(value = "X-API-Key", required = false) String apiKey,
            @RequestParam(required = false) String accountType) {
        return pashaBankAccountService.getAccountsByApiKey(apiKey, accountType);
    }
}