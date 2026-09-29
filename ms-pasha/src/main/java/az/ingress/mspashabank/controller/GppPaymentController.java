package az.ingress.mspashabank.controller;

import az.ingress.mspashabank.dto.gpp.*;
import az.ingress.mspashabank.service.GppPaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/payment-gpp")
@RequiredArgsConstructor
public class GppPaymentController {

    private final GppPaymentService gppPaymentService;

    @PostMapping
    public ResponseEntity<GppPaymentResponseDto> createPayment(@RequestBody GppPaymentRequestDto request) {
        GppPaymentResponseDto response = gppPaymentService.createPayment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/commission/calculation")
    public ResponseEntity<CommissionResponseDto> calculateCommission(
             @RequestBody CalculateCommissionRequestDto request) {
        return ResponseEntity.ok(gppPaymentService.calculateCommission(request));
    }

    @PostMapping("/invoice")
    public ResponseEntity<GetInvoiceInfoResponseDto> getInvoiceInfo(
            @RequestBody InvoiceRequestDto request) {
        return ResponseEntity.ok(gppPaymentService.getInvoiceInfo(request));
    }

    @GetMapping("/merchants")
    public ResponseEntity<List<MerchantResponseDto>> getMerchants() {
        return ResponseEntity.ok(gppPaymentService.getMerchants());
    }
}