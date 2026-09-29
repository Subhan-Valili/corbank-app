package az.corbank.abb.domain.model;

import java.math.BigDecimal;

/** One payment's status within a batch — spec §4.7/§4.8/§4.13. status is SUCCESS|IN_PROGRESS|ERROR|FAILURE (§3.7). */
public record PaymentLine(
        String paymentId,
        String transactionReference,
        String status,
        BigDecimal amount,
        String recipientAccount,
        String paymentTime
) {
}
