package az.corbank.abb.adapter.out.abbclient.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;

/** GET /payments/account/balance — spec §4.9. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AbbBalanceResponse(
        String accountNumber,
        String currency,
        BigDecimal availableBalance
) {
}
