package az.corbank.mscorbank.application.service;

import az.corbank.mscorbank.application.port.out.BankUnavailableException;
import az.corbank.mscorbank.application.port.out.CustomerRegistryPort;
import az.corbank.mscorbank.application.port.out.BankGateway;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
class BankCustomerRegistry implements CustomerRegistryPort {

    private final BankGateways banks;

    BankCustomerRegistry(BankGateways banks) {
        this.banks = banks;
    }

    @Override
    public boolean existsByPin(String pin) {
        BankUnavailableException lastFailure = null;
        for (BankGateway gateway : banks.all()) {
            try {
                if (gateway.customerExists(pin)) return true;
            } catch (BankUnavailableException e) {
                log.warn("{} PIN yoxlamasına cavab vermədi: {}", gateway.bank(), e.getMessage());
                lastFailure = e;
            }
        }
        // heç yerdə tapılmadı; amma bank əlçatmaz idisə "yoxdur" demək düzgün deyil
        if (lastFailure != null) throw lastFailure;
        return false;
    }
}
