package az.corbank.abb.domain.model;

import java.math.BigDecimal;

/**
 * What's needed to submit a payment — either an internal transfer between the client's own
 * ABB accounts, or a transfer to an external beneficiary. Maps onto ABB's Payment Order XML
 * (spec §5.1) by AbbHttpGateway; AbbMockGateway instead simulates it directly against the
 * mock_account/mock_operation Postgres tables.
 */
public record PaymentInstruction(
        String fromAccountNumber,
        String beneficiaryAccountNumber,
        String beneficiaryName,
        BigDecimal amount,
        String currency,
        String description,
        BigDecimal creditAmount,     // nullable: alıcı hesabın valyutasında düşəcək məbləğ (konvertasiya olunubsa)
        String creditCurrency
) {
}
