package az.corbank.mscorbank.adapter.out.pasha;

import az.corbank.mscorbank.domain.model.FxRate;
import az.corbank.mscorbank.application.port.out.FxRatePort;
import az.corbank.mscorbank.adapter.out.pasha.client.BulkControllerFeignClient;
import az.corbank.mscorbank.adapter.out.pasha.dto.bulk.FxRatesResponse;
import feign.FeignException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/** Outbound adapter: FX rates from ms-pasha, with indicative fallback rates when it is unreachable. */
@Component
@Slf4j
class PashaFxRateAdapter implements FxRatePort {

    // Keeps the table non-empty and stops cross-currency transfers from silently going 1:1
    private static final List<FxRate> FALLBACK_RATES = List.of(
            new FxRate("USD", new BigDecimal("1.6900"), new BigDecimal("1.7000")),
            new FxRate("EUR", new BigDecimal("1.9700"), new BigDecimal("1.9900")));

    private final BulkControllerFeignClient bulkClient;

    PashaFxRateAdapter(BulkControllerFeignClient bulkClient) {
        this.bulkClient = bulkClient;
    }

    @Override
    public List<FxRate> findRates() {
        try {
            FxRatesResponse rates = bulkClient.getFxRates();
            if (rates == null || rates.getFxRates() == null) return FALLBACK_RATES;
            return rates.getFxRates().stream()
                    .map(r -> new FxRate(r.getCurrency(), r.getStandardBuyRate(), r.getStandardSellRate()))
                    .toList();
        } catch (FeignException e) {
            log.warn("ms-pasha FX məzənnələrini vermədi, demo məzənnələr göstərilir: {}", e.getMessage());
            return FALLBACK_RATES;
        }
    }
}
