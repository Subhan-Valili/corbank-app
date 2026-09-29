package az.corbank.abb.adapter.out.abbclient.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AbbCurrencyRateDto(
        String rateType,
        String branchCode,
        String ccy1,
        String midRate,
        String buyRate,
        String saleRate
) {
}
