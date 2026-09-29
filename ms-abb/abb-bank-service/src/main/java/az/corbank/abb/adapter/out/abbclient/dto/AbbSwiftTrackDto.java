package az.corbank.abb.adapter.out.abbclient.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AbbSwiftTrackDto(
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
