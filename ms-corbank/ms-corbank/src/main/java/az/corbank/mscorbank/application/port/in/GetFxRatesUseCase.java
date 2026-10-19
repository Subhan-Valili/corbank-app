package az.corbank.mscorbank.application.port.in;

import az.corbank.mscorbank.domain.model.FxRate;

import java.util.List;

public interface GetFxRatesUseCase {

    List<FxRate> getRates();
}
