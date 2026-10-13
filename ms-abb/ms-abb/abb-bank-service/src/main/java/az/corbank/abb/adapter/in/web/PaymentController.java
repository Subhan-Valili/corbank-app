package az.corbank.abb.adapter.in.web;

import az.corbank.abb.adapter.in.web.dto.PaymentSubmitRequest;
import az.corbank.abb.adapter.in.web.dto.PaymentSubmitResponse;
import az.corbank.abb.application.port.in.SubmitPaymentUseCase;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/abb/payments")
class PaymentController {

    private final SubmitPaymentUseCase submitPayment;

    PaymentController(SubmitPaymentUseCase submitPayment) {
        this.submitPayment = submitPayment;
    }

    /** POST /internal/abb/payments — spec §4.3 (or the mock equivalent). */
    @PostMapping
    PaymentSubmitResponse submit(@Valid @RequestBody PaymentSubmitRequest request) {
        return PaymentSubmitResponse.from(submitPayment.submit(request.toDomain()));
    }
}
