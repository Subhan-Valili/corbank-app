package az.corbank.mscorbank.application.service;

import az.corbank.mscorbank.application.port.in.GetFxRatesUseCase;
import az.corbank.mscorbank.application.port.out.FxRatePort;
import az.corbank.mscorbank.domain.model.FxRate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
class FxRateService implements GetFxRatesUseCase {

    private final FxRatePort fxRates;

    FxRateService(FxRatePort fxRates) {
        this.fxRates = fxRates;
    }

    @Override
    public List<FxRate> getRates() {
        return fxRates.findRates();
    }
}
