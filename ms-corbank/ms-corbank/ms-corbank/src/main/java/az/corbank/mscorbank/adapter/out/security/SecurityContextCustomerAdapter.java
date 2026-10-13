package az.corbank.mscorbank.adapter.out.security;

import az.corbank.mscorbank.adapter.out.persistence.entity.UserEntity;
import az.corbank.mscorbank.adapter.out.persistence.repository.jpa.UserRepository;
import az.corbank.mscorbank.application.port.out.CurrentCustomerPort;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

/** JWT-dən gələn email -> DB-dəki istifadəçi -> şifrəsi açılmış PIN. */
@Component
@RequiredArgsConstructor
class SecurityContextCustomerAdapter implements CurrentCustomerPort {

    private final UserRepository userRepository;
    private final PinProtector pinProtector;

    @Override
    public Optional<String> currentPin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return Optional.empty();
        }
        return userRepository.findByEmail(auth.getName())
                .map(UserEntity::getPinEncrypted)
                .filter(p -> !p.isBlank())
                .map(pinProtector::decrypt);
    }
}
