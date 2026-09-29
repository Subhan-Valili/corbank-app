package az.corbank.mscorbank.adapter.in.web.passthrough;

import az.corbank.mscorbank.adapter.out.pasha.dto.gpp.*;
import az.corbank.mscorbank.adapter.out.pasha.client.GppApiFeignClient;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/payment-gpp")
@RequiredArgsConstructor
public class GppPaymentController {

    private final GppApiFeignClient gppApiFeignClient;

    @PostMapping
    public ResponseEntity<GppPaymentResponseDto> createPayment(
            @RequestBody GppPaymentRequestDto request) {
        return ResponseEntity.ok(gppApiFeignClient.createPayment(request));
    }

    @PostMapping("/commission/calculation")
    public ResponseEntity<CommissionResponseDto> calculateCommission(
            @RequestBody CalculateCommissionRequestDto request) {
        return ResponseEntity.ok(gppApiFeignClient.calculateCommission(request));
    }

    @PostMapping("/invoice")
    public ResponseEntity<GetInvoiceInfoResponseDto> getInvoiceInfo(
            @RequestBody InvoiceRequestDto request) {
        return ResponseEntity.ok(gppApiFeignClient.getInvoiceInfo(request));
    }

    @GetMapping("/merchants")
    public ResponseEntity<List<MerchantResponseDto>> getMerchants() {
        return ResponseEntity.ok(gppApiFeignClient.getMerchants());
    }
}
