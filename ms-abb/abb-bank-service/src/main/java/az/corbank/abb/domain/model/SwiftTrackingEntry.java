package az.corbank.abb.domain.model;

import java.math.BigDecimal;

/** spec §4.20. */
public record SwiftTrackingEntry(
        long id,
        String contractRefNo,
        String processDate,
        String track,
        String bicCode,
        String bicName,
        String status,
        String statusDetail,
        BigDecimal amount,
        String currency,
        BigDecimal amountCharge,
        String currencyCharge,
        BigDecimal exchangeRate
) {
}
