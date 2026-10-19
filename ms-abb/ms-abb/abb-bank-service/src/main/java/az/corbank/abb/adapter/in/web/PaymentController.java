package az.corbank.abb.adapter.in.web;

import az.corbank.abb.adapter.in.web.dto.PaymentSubmitRequest;
import az.corbank.abb.adapter.in.web.dto.PaymentSubmitResponse;
import az.corbank.abb.application.port.in.SubmitPaymentUseCase;
import az.corbank.abb.application.service.AbbApiKeyService;
import org.springframework.web.bind.annotation.RequestHeader;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/abb/payments")
class PaymentController {

    private final SubmitPaymentUseCase submitPayment;
    private final AbbApiKeyService apiKeys;

    PaymentController(SubmitPaymentUseCase submitPayment, AbbApiKeyService apiKeys) {
        this.submitPayment = submitPayment;
        this.apiKeys = apiKeys;
    }

    /** POST /internal/abb/payments — spec §4.3 (or the mock equivalent). */
    @PostMapping
    PaymentSubmitResponse submit(
            @RequestHeader(value = "X-API-Key", required = false) String apiKey,
            @Valid @RequestBody PaymentSubmitRequest request) {
        return PaymentSubmitResponse.from(submitPayment.submit(apiKeys.authenticate(apiKey), request.toDomain()));
    }
}
