package az.corbank.mscorbank.adapter.out.pasha.client;

import az.corbank.mscorbank.adapter.out.pasha.dto.bulk.*;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "ms-pasha-bulk-client", url = "${ms-pasha.base-url}")
public interface BulkControllerFeignClient {

    @PostMapping("/api/v1/b2b-payment/payments")
    CreateBulkPaymentResponse createBulkPayments(@RequestBody CreateBulkPaymentRequest request);

    @PostMapping("/api/v1/b2b-payment/payments/approve")
    void approveBulkPayments(@RequestBody ApproveBulkPaymentRequest request);

    @GetMapping("/api/v1/b2b-payment/payments/bulk-id/{referenceNumber}")
    GetBulkIdResponse getBulkIdByReferenceNumber(@PathVariable("referenceNumber") String referenceNumber);

    @GetMapping("/api/v1/b2b-payment/payments/fx/rates")
    FxRatesResponse getFxRates();
}