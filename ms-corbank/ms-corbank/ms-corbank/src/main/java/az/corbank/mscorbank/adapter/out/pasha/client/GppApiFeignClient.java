package az.corbank.mscorbank.adapter.out.pasha.client;


import az.corbank.mscorbank.adapter.out.pasha.dto.gpp.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "ms-pasha-gpp-client", url = "${ms-pasha.base-url}")
public interface GppApiFeignClient {

    @PostMapping("/api/v1/payment-gpp")
    GppPaymentResponseDto createPayment(@RequestBody GppPaymentRequestDto request);

    @PostMapping("/api/v1/payment-gpp/commission/calculation")
    CommissionResponseDto calculateCommission(@RequestBody CalculateCommissionRequestDto request);

    @PostMapping("/api/v1/payment-gpp/invoice")
    GetInvoiceInfoResponseDto getInvoiceInfo(@RequestBody InvoiceRequestDto request);

    @GetMapping("/api/v1/payment-gpp/merchants")
    List<MerchantResponseDto> getMerchants();
}