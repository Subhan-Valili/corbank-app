package az.corbank.abb.adapter.out.abbclient.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.util.List;

/** GET /payments/{batchNumber} and GET /payments/salary/{batchNumber} — spec §4.7, §4.13. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AbbBatchStatusResponse(
        String batchNumber,
        FileStatus status,
        List<PaymentItem> payments
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record FileStatus(String status, String description) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PaymentItem(
            String paymentId,
            String transactionReference,
            PaymentStatus paymentStatus,
            BigDecimal paymentAmount,
            String recipientAccount,
            String paymentTime
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PaymentStatus(String status, String transactionReference) {
    }
}
