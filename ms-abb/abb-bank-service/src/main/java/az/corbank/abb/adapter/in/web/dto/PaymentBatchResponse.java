package az.corbank.abb.adapter.in.web.dto;

import az.corbank.abb.domain.model.PaymentBatch;
import az.corbank.abb.domain.model.PaymentLine;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record PaymentBatchResponse(
        String batchNumber,
        String externalReference,
        String batchType,
        String status,
        String statusDescription,
        Instant submittedAt,
        Instant lastCheckedAt,
        List<Line> payments
) {
    public record Line(String paymentId, String transactionReference, String status,
                        BigDecimal amount, String recipientAccount, String paymentTime) {
        static Line from(PaymentLine p) {
            return new Line(p.paymentId(), p.transactionReference(), p.status(),
                    p.amount(), p.recipientAccount(), p.paymentTime());
        }
    }

    public static PaymentBatchResponse from(PaymentBatch b) {
        return new PaymentBatchResponse(
                b.batchNumber(), b.externalReference(), b.type().name(), b.status().name(),
                b.statusDescription(), b.submittedAt(), b.lastCheckedAt(),
                b.payments().stream().map(Line::from).toList());
    }
}
