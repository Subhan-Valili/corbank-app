package az.corbank.mscorbank.adapter.in.web.dto;

import java.math.BigDecimal;

/**
 * corbank-frontend js/account.js-dəki bütün ödəniş sehrbazlarının (domestic, international,
 * instant, budget, utility, own-transfer, FX, bulk CSV) ortaq çıxış formasıdır.
 */
public record PaymentRequestDto(
        String fromAccountId,
        String beneficiaryName,
        String beneficiaryIban,
        BigDecimal amount,
        String currency,
        String description,
        BigDecimal creditAmount,     // öz hesablar arası / valyuta mübadiləsi: alıcı hesaba düşəcək (konvertasiya olunmuş) məbləğ
        String creditCurrency
) {
}
