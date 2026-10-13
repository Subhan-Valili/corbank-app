package az.corbank.abb.adapter.in.web.dto;

import az.corbank.abb.domain.model.PaymentOutcome;

public record PaymentSubmitResponse(
        String status,   // "COMPLETED" | "REJECTED"
        String message,
        String reference
) {
    public static PaymentSubmitResponse from(PaymentOutcome outcome) {
        return new PaymentSubmitResponse(outcome.completed() ? "COMPLETED" : "REJECTED",
                outcome.message(), outcome.reference());
    }
}
