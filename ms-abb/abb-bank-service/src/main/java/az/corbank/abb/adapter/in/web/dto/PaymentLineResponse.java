package az.corbank.abb.adapter.in.web.dto;

import az.corbank.abb.domain.model.PaymentLine;

import java.math.BigDecimal;

public record PaymentLineResponse(
        String paymentId,
        String transactionReference,
        String status,
        BigDecimal amount,
        String recipientAccount,
        String paymentTime
) {
    public static PaymentLineResponse from(PaymentLine p) {
        return new PaymentLineResponse(p.paymentId(), p.transactionReference(), p.status(),
                p.amount(), p.recipientAccount(), p.paymentTime());
    }
}
