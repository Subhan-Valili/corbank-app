package az.corbank.abb.domain.model;

/** spec §4.18. */
public record CurrencyRate(String rateType, String branchCode, String currency, String midRate, String buyRate, String saleRate) {
}
