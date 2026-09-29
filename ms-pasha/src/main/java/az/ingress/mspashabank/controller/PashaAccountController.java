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

    @GetMapping("/{accountId}")
    public PashaAccountResponseDto findAccountByAccountId(@PathVariable String accountId) {
        return pashaBankAccountService.getAccountByAccountId(accountId);
    }

    @GetMapping("/iban/{iban}")
    public PashaAccountResponseDto findAccountByIban(@PathVariable String iban) {
        return pashaBankAccountService.getAccountByIban(iban);
    }
}
