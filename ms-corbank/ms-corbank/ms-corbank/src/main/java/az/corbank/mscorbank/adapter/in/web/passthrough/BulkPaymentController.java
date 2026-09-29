package az.corbank.mscorbank.adapter.in.web.passthrough;

import az.corbank.mscorbank.adapter.out.pasha.dto.bulk.*;
import az.corbank.mscorbank.adapter.out.pasha.client.BulkControllerFeignClient;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/b2b-payment/payments")
@RequiredArgsConstructor
public class BulkPaymentController {

    private final BulkControllerFeignClient bulkControllerFeignClient;

    @PostMapping
    public ResponseEntity<CreateBulkPaymentResponse> createBulkPayments(
            @RequestBody CreateBulkPaymentRequest request) {
        return ResponseEntity.ok(bulkControllerFeignClient.createBulkPayments(request));
    }

    @PostMapping("/approve")
    public ResponseEntity<Void> approveBulkPayments(
            @RequestBody ApproveBulkPaymentRequest request) {
        bulkControllerFeignClient.approveBulkPayments(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/bulk-id/{referenceNumber}")
    public ResponseEntity<GetBulkIdResponse> getBulkIdByReferenceNumber(
            @PathVariable("referenceNumber") String referenceNumber) {
        return ResponseEntity.ok(bulkControllerFeignClient.getBulkIdByReferenceNumber(referenceNumber));
    }

    @GetMapping("/fx/rates")
    public ResponseEntity<FxRatesResponse> getFxRates() {
        return ResponseEntity.ok(bulkControllerFeignClient.getFxRates());
    }
}
