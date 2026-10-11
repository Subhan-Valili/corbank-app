package az.corbank.mscorbank.application.service.auth;

import az.corbank.mscorbank.adapter.in.web.dto.auth.LoginRequest;
import az.corbank.mscorbank.adapter.in.web.dto.auth.LoginResponse;
import az.corbank.mscorbank.adapter.in.web.dto.auth.RefreshRequest;
import az.corbank.mscorbank.adapter.out.persistence.entity.RefreshToken;
import az.corbank.mscorbank.adapter.out.persistence.repository.redis.RefreshTokenRepository;
import az.corbank.mscorbank.adapter.out.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginService {

    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;

    public LoginResponse login(LoginRequest request) {
        String email = request.email().trim().toLowerCase();
        log.info("Login attempt: {}", mask(email));

        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, request.password()));
        } catch (BadCredentialsException ex) {
            log.warn("Login failed. Invalid credentials: {}", mask(email));
            throw ex;
        }

        // authenticate() artıq user-i yükləyib, DB-yə ikinci sorğu getmirik
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        String accessToken = jwtService.generateToken(userDetails);
        String refreshToken = createRefreshToken(email);

        log.info("User logged in: {}", mask(email));
        return new LoginResponse(accessToken, refreshToken);
    }

    public LoginResponse refresh(RefreshRequest request) {
        RefreshToken stored = refreshTokenRepository.findById(request.refreshToken())
                .orElseThrow(() -> {
                    log.warn("Refresh failed. Token not found or expired");
                    return new RuntimeException("Refresh token etibarsızdır və ya vaxtı bitib");
                });

        UserDetails userDetails = userDetailsService.loadUserByUsername(stored.getEmail());

        String accessToken = jwtService.generateToken(userDetails);

        // köhnə refresh token bir dəfəlikdir, yenisi verilir
        refreshTokenRepository.delete(stored);
        String newRefreshToken = createRefreshToken(stored.getEmail());

        log.info("Token refreshed: {}", mask(stored.getEmail()));
        return new LoginResponse(accessToken, newRefreshToken);
    }

    private String createRefreshToken(String email) {
        refreshTokenRepository.findByEmail(email).ifPresent(old -> {
            log.info("Old refresh token deleted: {}", mask(email));
            refreshTokenRepository.delete(old);
        });

        String id = UUID.randomUUID().toString();
        refreshTokenRepository.save(RefreshToken.builder()
                .id(id)
                .email(email)
                .ttl(jwtService.getRefreshExpirationInSeconds())
                .build());
        return id;
    }

    private String mask(String email) {
        int at = email.indexOf('@');
        return at <= 1 ? "***" : email.charAt(0) + "***" + email.substring(at);
    }
}