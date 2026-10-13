package az.corbank.abb.config;

import az.corbank.abb.application.port.out.AbbBankGateway;
import az.corbank.abb.application.port.out.OperationHistoryRepositoryPort;
import az.corbank.abb.application.service.AccountApplicationService;
import az.corbank.abb.application.service.PaymentApplicationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * The application services in az.corbank.abb.application.service are deliberately plain
 * Java classes with no @Service/@Component annotation — the application layer shouldn't
 * need to know it's running inside Spring. This is the composition root that wires them
 * up instead, injecting whichever adapter Spring found for each outbound port.
 */
@Configuration
public class UseCaseConfig {

    @Bean
    public AccountApplicationService accountApplicationService(AbbBankGateway gateway,
                                                                  OperationHistoryRepositoryPort historyRepository) {
        return new AccountApplicationService(gateway, historyRepository);
    }

    @Bean
    public PaymentApplicationService paymentApplicationService(AbbBankGateway gateway) {
        return new PaymentApplicationService(gateway);
    }
}
