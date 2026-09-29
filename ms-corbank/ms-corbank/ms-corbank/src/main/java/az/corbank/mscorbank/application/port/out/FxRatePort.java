package az.corbank.mscorbank.application.port.out;

import az.corbank.mscorbank.domain.model.FxRate;

import java.util.List;

public interface FxRatePort {

    /** Always returns usable rates (implementations fall back to indicative rates when the source is down). */
    List<FxRate> findRates();
}
