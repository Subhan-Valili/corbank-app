package az.corbank.mscorbank.adapter.out.security;

import az.corbank.mscorbank.adapter.out.persistence.entity.UserEntity;
import az.corbank.mscorbank.adapter.out.persistence.repository.jpa.UserRepository;
import az.corbank.mscorbank.application.port.out.BankApiKeyStorePort;
import az.corbank.mscorbank.application.port.out.CurrentCustomerPort;
import az.corbank.mscorbank.domain.model.Bank;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

/** JWT-dən gələn email -> DB-dəki istifadəçi -> şifrəsi açılmış PIN / bank API Key-i. */
@Component
@RequiredArgsConstructor
class SecurityContextCustomerAdapter implements CurrentCustomerPort {

    private final UserRepository userRepository;
    private final PinProtector pinProtector;
    private final BankApiKeyStorePort apiKeyStore;

    @Override
    public Optional<String> currentEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return Optional.empty();
        }
        return Optional.ofNullable(auth.getName());
    }

    @Override
    public Optional<String> currentPin() {
        return currentEmail()
                .flatMap(userRepository::findByEmail)
                .map(UserEntity::getPinEncrypted)
                .filter(p -> !p.isBlank())
                .map(pinProtector::decrypt);
    }

    @Override
    public Optional<String> currentApiKey(Bank bank) {
        return currentEmail().flatMap(email -> apiKeyStore.find(email, bank));
    }
}
