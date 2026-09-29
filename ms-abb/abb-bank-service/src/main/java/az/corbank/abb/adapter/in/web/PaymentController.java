package az.corbank.abb.adapter.in.web;

import az.corbank.abb.adapter.in.web.dto.*;
import az.corbank.abb.application.port.in.*;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/internal/abb/payments")
class PaymentController {

    private final SubmitPaymentUseCase submitPayment;
    private final VerifyOtpUseCase verifyOtp;
    private final GetBatchStatusUseCase getBatchStatus;
    private final GetIndividualPaymentUseCase getIndividualPayment;
    private final GetFileStatusUseCase getFileStatus;
    private final ListPaymentBatchesUseCase listBatches;

    PaymentController(SubmitPaymentUseCase submitPayment, VerifyOtpUseCase verifyOtp,
                       GetBatchStatusUseCase getBatchStatus, GetIndividualPaymentUseCase getIndividualPayment,
                       GetFileStatusUseCase getFileStatus, ListPaymentBatchesUseCase listBatches) {
        this.submitPayment = submitPayment;
        this.verifyOtp = verifyOtp;
        this.getBatchStatus = getBatchStatus;
        this.getIndividualPayment = getIndividualPayment;
        this.getFileStatus = getFileStatus;
        this.listBatches = listBatches;
    }

    /** POST /internal/abb/payments — "type" picks regular/signed/otp/salary(-signed) — spec §4.2-4.4, §4.11-4.12. */
    @PostMapping
    PaymentBatchResponse submit(@Valid @RequestBody PaymentSubmitRequest request) {
        return PaymentBatchResponse.from(submitPayment.submit(request.toDomain()));
    }

    /** POST /internal/abb/payments/verify-otp — spec §4.5. */
    @PostMapping("/verify-otp")
    VerifyOtpResponse verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        return new VerifyOtpResponse(verifyOtp.verifyOtp(request.batchNumber(), request.otpCode()));
    }

    /** GET /internal/abb/payments/{batchNumber} — spec §4.7/§4.13. */
    @GetMapping("/{batchNumber}")
    PaymentBatchResponse getBatchStatus(@PathVariable String batchNumber) {
        return PaymentBatchResponse.from(getBatchStatus.getBatchStatus(batchNumber));
    }

    /** GET /internal/abb/payments/{batchNumber}/{paymentId} — spec §4.8. */
    @GetMapping("/{batchNumber}/{paymentId}")
    PaymentLineResponse getIndividualPayment(@PathVariable String batchNumber, @PathVariable String paymentId) {
        return PaymentLineResponse.from(getIndividualPayment.getPayment(batchNumber, paymentId));
    }

    /** GET /internal/abb/payments/file-status?externalReference= — spec §4.6. */
    @GetMapping("/file-status")
    FileStatusResponse getFileStatus(@RequestParam String externalReference) {
        return FileStatusResponse.from(getFileStatus.getFileStatus(externalReference));
    }

    /** GET /internal/abb/payments — every batch we've submitted, newest first (local read, no ABB call). */
    @GetMapping
    List<PaymentBatchResponse> listBatches() {
        return listBatches.listBatches().stream().map(PaymentBatchResponse::from).toList();
    }
}
