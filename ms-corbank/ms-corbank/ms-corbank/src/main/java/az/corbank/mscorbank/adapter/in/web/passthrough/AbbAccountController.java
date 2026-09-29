package az.corbank.mscorbank.adapter.in.web.passthrough;

import az.corbank.mscorbank.adapter.out.abb.dto.AbbAccountResponseDto;
import az.corbank.mscorbank.adapter.out.abb.dto.AbbStatementResponseDto;
import az.corbank.mscorbank.adapter.out.abb.client.AbbAccountFeignClient;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/v1/abb/accounts")
@RequiredArgsConstructor
public class AbbAccountController {

    private final AbbAccountFeignClient abbAccountFeignClient;

    @GetMapping
    public ResponseEntity<List<AbbAccountResponseDto>> findAllAccounts() {
        return ResponseEntity.ok(abbAccountFeignClient.findAllAccounts());
    }

    @GetMapping("/{accountNumber}/statement")
    public ResponseEntity<AbbStatementResponseDto> getStatement(
            @PathVariable("accountNumber") String accountNumber,
            @RequestParam("fromDate") String fromDate,
            @RequestParam("toDate") String toDate,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "pageSize", defaultValue = "20") int pageSize) {
        return ResponseEntity.ok(abbAccountFeignClient.getStatement(accountNumber, fromDate, toDate, page, pageSize));
    }
}
