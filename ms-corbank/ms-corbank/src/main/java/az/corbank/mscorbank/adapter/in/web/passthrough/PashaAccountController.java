package az.corbank.mscorbank.adapter.in.web.passthrough;

import az.corbank.mscorbank.adapter.out.pasha.dto.account.PashaAccountResponseDto;
import az.corbank.mscorbank.adapter.out.pasha.client.PashaAccountFeignClient;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
public class PashaAccountController {

    private final PashaAccountFeignClient pashaAccountFeignClient;

    @GetMapping
    public ResponseEntity<List<PashaAccountResponseDto>> findAllAccountsByCustomerNo(
            @RequestParam("customerNo") String customerNo) {
        return ResponseEntity.ok(pashaAccountFeignClient.findAllAccountsByCustomerNo(customerNo));
    }

    @GetMapping("/by-pin")
    public ResponseEntity<List<PashaAccountResponseDto>> findAllAccountsByPin(
            @RequestHeader("X-Pin") String pin) {
        return ResponseEntity.ok(pashaAccountFeignClient.findAllAccountsByPin(pin));
    }

    @GetMapping("/by-tin")
    public ResponseEntity<List<PashaAccountResponseDto>> findAllAccountsByTin(
            @RequestParam("tin") String tin) {
        return ResponseEntity.ok(pashaAccountFeignClient.findAllAccountsByTin(tin));
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<PashaAccountResponseDto> findAccountByAccountId(
            @PathVariable("accountId") String accountId) {
        return ResponseEntity.ok(pashaAccountFeignClient.findAccountByAccountId(accountId));
    }

    @GetMapping("/iban/{iban}")
    public ResponseEntity<PashaAccountResponseDto> findAccountByIban(
            @PathVariable("iban") String iban) {
        return ResponseEntity.ok(pashaAccountFeignClient.findAccountByIban(iban));
    }
}