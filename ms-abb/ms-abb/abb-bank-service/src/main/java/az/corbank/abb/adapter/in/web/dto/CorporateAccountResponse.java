package az.corbank.abb.adapter.in.web.dto;

import az.corbank.abb.domain.model.CorporateAccount;

import java.math.BigDecimal;

public record CorporateAccountResponse(
        String name,
        String currency,
        String accountNumber,
        String iban,
        BigDecimal availableBalance
) {
    public static CorporateAccountResponse from(CorporateAccount a) {
        return new CorporateAccountResponse(a.name(), a.currency(), a.accountNumber(), a.iban(), a.availableBalance());
    }
}
