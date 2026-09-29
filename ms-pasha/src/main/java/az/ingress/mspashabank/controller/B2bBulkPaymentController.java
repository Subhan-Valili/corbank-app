package az.ingress.mspashabank.controller;

import az.ingress.mspashabank.dto.bulk.*;
import az.ingress.mspashabank.service.B2bBulkPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/b2b-payment/payments")
@RequiredArgsConstructor
public class B2bBulkPaymentController {

    private final B2bBulkPaymentService bulkPaymentService;

    @PostMapping
    public ResponseEntity<CreateBulkPaymentResponse> createBulkPayments(@RequestBody CreateBulkPaymentRequest request) {
        return ResponseEntity.ok(bulkPaymentService.createBulkPayments(request));
    }

    @PostMapping("/approve")
    public ResponseEntity<Void> approveBulkPayments(@RequestBody ApproveBulkPaymentRequest request) {
        bulkPaymentService.approveBulkPayments(request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/bulk-id/{referenceNumber}")
    public ResponseEntity<GetBulkIdResponse> getBulkIdByReferenceNumber(
            @PathVariable String referenceNumber){
        return ResponseEntity.ok(bulkPaymentService.getBulkIdByReferenceNumber(referenceNumber));
    }

    @GetMapping("/fx/rates")
    public ResponseEntity<FxRatesResponse> getFxRates() {
        return ResponseEntity.ok(bulkPaymentService.getFxRates());
    }

}