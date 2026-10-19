package az.corbank.abb.config;

import az.corbank.abb.adapter.out.persistence.AbbApiKeyJpaEntity;
import az.corbank.abb.adapter.out.persistence.AbbApiKeyJpaRepository;
import az.corbank.abb.application.service.AbbApiKeyService;
import az.corbank.abb.domain.model.CustomerIdentity;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/** Deterministic keys ONLY for the local ABB mock dataset; never enabled in real-bank mode. */
@Component
@ConditionalOnProperty(prefix = "abb.api", name = {"mock-enabled", "seed-demo-keys"}, havingValue = "true")
class AbbDemoApiKeys {
    private final AbbApiKeyJpaRepository repository;

    AbbDemoApiKeys(AbbApiKeyJpaRepository repository) {
        this.repository = repository;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void seed() {
        add("abb_test_subhan_410001", new CustomerIdentity("5SV1A23", "+994501110001"));
        add("abb_test_ferid_410003", new CustomerIdentity("7FA3C45", "+994501110003"));
        add("abb_test_imran_410004", new CustomerIdentity("8IY4D56", "+994501110004"));
    }

    private void add(String raw, CustomerIdentity customer) {
        String hash = AbbApiKeyService.sha256(raw);
        if (!repository.existsByKeyHash(hash)) {
            repository.save(new AbbApiKeyJpaEntity(hash, raw.substring(0, 10),
                    customer.pin(), customer.mobileNumber(), null));
        }
    }
}
