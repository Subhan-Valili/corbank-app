package az.corbank.mscorbank.adapter.out.pasha.client;

import az.corbank.mscorbank.adapter.out.pasha.dto.account.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * ms-pasha (PashaAccountController) üçün Feign client.
 * Base URL "ms-pasha.base-url" property-si ilə verilir (bax: application.yml).
 */
@FeignClient(name = "ms-pasha-account-client", url = "${ms-pasha.base-url}")
public interface PashaAccountFeignClient {

    @GetMapping("/api/v1/accounts")
    List<PashaAccountResponseDto> findAllAccountsByCustomerNo(@RequestParam("customerNo") String customerNo);

    @GetMapping("/api/v1/accounts/{accountId}")
    PashaAccountResponseDto findAccountByAccountId(@PathVariable("accountId") String accountId);

    @GetMapping("/api/v1/accounts/iban/{iban}")
    PashaAccountResponseDto findAccountByIban(@PathVariable("iban") String iban);
}

