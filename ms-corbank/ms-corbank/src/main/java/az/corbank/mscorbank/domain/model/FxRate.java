package az.corbank.mscorbank.domain.model;

import java.math.BigDecimal;

public record FxRate(String currency, BigDecimal buy, BigDecimal sell) {
}
