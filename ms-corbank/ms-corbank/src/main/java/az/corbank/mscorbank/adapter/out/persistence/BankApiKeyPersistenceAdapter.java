package az.corbank.mscorbank.adapter.out.persistence;

import az.corbank.mscorbank.adapter.out.persistence.entity.BankApiKeyEntity;
import az.corbank.mscorbank.adapter.out.persistence.entity.UserEntity;
import az.corbank.mscorbank.adapter.out.persistence.repository.jpa.BankApiKeyRepository;
import az.corbank.mscorbank.adapter.out.persistence.repository.jpa.UserRepository;
import az.corbank.mscorbank.adapter.out.security.PinProtector;
import az.corbank.mscorbank.application.port.out.BankApiKeyStorePort;
import az.corbank.mscorbank.domain.model.Bank;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/** API Key-lər DB-də yalnız şifrələnmiş saxlanır (PinProtector-un AES-256-GCM-i). */
@Component
@RequiredArgsConstructor
class BankApiKeyPersistenceAdapter implements BankApiKeyStorePort {

    private final BankApiKeyRepository repository;
    private final UserRepository userRepository;
    private final PinProtector protector;

    @Override
    @Transactional
    public void saveAll(String email, Map<Bank, String> apiKeysByBank) {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("İstifadəçi tapılmadı"));
        apiKeysByBank.forEach((bank, apiKey) -> {
            BankApiKeyEntity entity = repository.findByUser_EmailAndBank(email, bank)
                    .orElseGet(() -> BankApiKeyEntity.builder().user(user).bank(bank).build());
            entity.setApiKeyEncrypted(protector.encrypt(apiKey));
            repository.save(entity);
        });
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<String> find(String email, Bank bank) {
        return repository.findByUser_EmailAndBank(email, bank)
                .map(BankApiKeyEntity::getApiKeyEncrypted)
                .map(protector::decrypt);
    }

    @Override
    @Transactional(readOnly = true)
    public Set<Bank> connectedBanks(String email) {
        Set<Bank> result = EnumSet.noneOf(Bank.class);
        repository.findAllByUser_Email(email).forEach(e -> result.add(e.getBank()));
        return result;
    }

    @Override
    @Transactional
    public boolean delete(String email, Bank bank) {
        return repository.deleteByUser_EmailAndBank(email, bank) > 0;
    }
}
