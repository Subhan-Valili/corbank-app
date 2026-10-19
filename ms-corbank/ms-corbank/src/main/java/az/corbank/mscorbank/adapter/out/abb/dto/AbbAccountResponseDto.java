package az.corbank.mscorbank.adapter.out.abb.dto;

import lombok.Builder;

import java.math.BigDecimal;

/**
 * ms-abb-bank-in (mənim ABB Bank inteqrasiya mikroservisim, bax abb-bank-service/README.md)
 * "GET /internal/abb/accounts" cavabının güzgüsü — ABB-nin "corporate-account-info"
 * endpoint-inə (§4.21) uyğun gəlir.
 */
@Builder
public record AbbAccountResponseDto(
        String name,
        String currency,
        String accountNumber,
        String iban,
        BigDecimal availableBalance
) {
}
