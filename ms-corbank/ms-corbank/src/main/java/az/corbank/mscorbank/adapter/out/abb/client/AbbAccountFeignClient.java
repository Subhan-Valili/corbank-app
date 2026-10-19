package az.corbank.mscorbank.adapter.out.abb.client;

import az.corbank.mscorbank.adapter.out.abb.dto.AbbAccountResponseDto;
import az.corbank.mscorbank.adapter.out.abb.dto.AbbOperationLineDto;
import az.corbank.mscorbank.adapter.out.abb.dto.AbbPaymentRequestDto;
import az.corbank.mscorbank.adapter.out.abb.dto.AbbPaymentResponseDto;
import az.corbank.mscorbank.adapter.out.abb.dto.AbbStatementResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;


@FeignClient(name = "ms-abb-account-client", url = "${ms-abb.base-url}")
public interface AbbAccountFeignClient {

    /** GET /internal/abb/accounts — bütün hesabların siyahısı (ABB spec §4.21). */
    @GetMapping("/internal/abb/accounts")
    List<AbbAccountResponseDto> findAllAccounts(@RequestHeader("X-Customer-Pin") String pin);

    @GetMapping("/internal/abb/accounts/byKey")
    List<AbbAccountResponseDto> findAccountsByApiKey(@RequestHeader("X-API-Key") String apiKey);


    /** ms-abb hesabın bu PIN-ə məxsus olduğunu yoxlayır (yoxsa 404 ACCOUNT_NOT_FOUND). */
    @GetMapping("/internal/abb/accounts/{accountNumber}/statement")
    AbbStatementResponseDto getStatement(
            @RequestHeader("X-Customer-Pin") String pin,
            @PathVariable("accountNumber") String accountNumber,
            @RequestParam("fromDate") String fromDate,
            @RequestParam("toDate") String toDate,
            @RequestParam("page") int page,
            @RequestParam("pageSize") int pageSize);

    @GetMapping("/internal/abb/accounts/{accountNumber}/statement")
    AbbStatementResponseDto getStatementByApiKey(
            @RequestHeader("X-API-Key") String apiKey,
            @PathVariable("accountNumber") String accountNumber,
            @RequestParam("fromDate") String fromDate,
            @RequestParam("toDate") String toDate,
            @RequestParam("page") int page,
            @RequestParam("pageSize") int pageSize);

    @GetMapping("/internal/abb/accounts/history/recent")
    List<AbbOperationLineDto> getRecentHistoryByApiKey(
            @RequestHeader("X-API-Key") String apiKey, @RequestParam("limit") int limit);

    /**
     * GET /internal/abb/accounts/history/recent — ms-abb-bank-ın öz DB-sindən oxuyur,
     * ABB-yə heç bir sorğu getmir. Dashboard-un "Son əməliyyatlar" bölməsi üçün ucuz yol.
     */
    @GetMapping("/internal/abb/accounts/history/recent")
    List<AbbOperationLineDto> getRecentHistory(@RequestHeader("X-Customer-Pin") String pin, @RequestParam("limit") int limit);

    /**
     * POST /internal/abb/payments — ms-abb-bank ya ABB-nin özünə göndərir (real rejim), ya da
     * mock_account/mock_operation cədvəllərini birbaşa dəyişdirir (mock rejim) — hər iki halda
     * balans/tarixçə dəyişikliyi ms-abb-bank-ın öz Postgres-ində baş verir.
     */
    @PostMapping("/internal/abb/payments")
    AbbPaymentResponseDto submitPayment(@RequestHeader("X-API-Key") String apiKey,
                                         @RequestBody AbbPaymentRequestDto request);
}
