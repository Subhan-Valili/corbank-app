package az.corbank.abb.config;

import az.corbank.abb.application.port.out.AbbBankGateway;
import az.corbank.abb.application.port.out.AccountSnapshotRepositoryPort;
import az.corbank.abb.application.port.out.PaymentBatchRepositoryPort;
import az.corbank.abb.application.service.AccountApplicationService;
import az.corbank.abb.application.service.PaymentApplicationService;
import az.corbank.abb.application.service.ReferenceDataApplicationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * The application services in az.corbank.abb.application.service are deliberately plain
 * Java classes with no @Service/@Component annotation — the application layer shouldn't
 * need to know it's running inside Spring. This is the composition root that wires them
 * up instead, injecting whichever adapters Spring found for each outbound port.
 */
@Configuration
public class UseCaseConfig {

    @Bean
    public AccountApplicationService accountApplicationService(AbbBankGateway gateway,
                                                                  AccountSnapshotRepositoryPort snapshotRepository) {
        return new AccountApplicationService(gateway, snapshotRepository);
    }

    @Bean
    public PaymentApplicationService paymentApplicationService(AbbBankGateway gateway,
                                                                  PaymentBatchRepositoryPort batchRepository) {
        return new PaymentApplicationService(gateway, batchRepository);
    }

    @Bean
    public ReferenceDataApplicationService referenceDataApplicationService(AbbBankGateway gateway) {
        return new ReferenceDataApplicationService(gateway);
    }
}
