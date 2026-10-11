package az.corbank.mscorbank.application.service.auth;

import az.corbank.mscorbank.adapter.in.web.dto.auth.*;
import az.corbank.mscorbank.adapter.out.persistence.entity.RoleEntity;
import az.corbank.mscorbank.adapter.out.persistence.entity.UserEntity;
import az.corbank.mscorbank.adapter.out.persistence.repository.jpa.RoleRepository;
import az.corbank.mscorbank.adapter.out.persistence.repository.jpa.UserRepository;

import az.corbank.mscorbank.application.port.out.EmailSenderPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegisterService {

    private static final String DEFAULT_ROLE = "ROLE_USER";
    private static final int MAX_ATTEMPTS = 3;
    private static final SecureRandom RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;
    private final EmailSenderPort emailSender;
    private final PasswordEncoder passwordEncoder;

    public RegisterResponse preRegisterUser(RegisterRequest request) {
        String email = normalize(request.email());
        log.info("Registration attempt: {}", mask(email));

        assertNotRegistered(email, request.mobileNumber());

        PendingRegistration pending = new PendingRegistration(
                request.name().trim(),
                request.surname().trim(),
                email,
                passwordEncoder.encode(request.password()),
                request.mobileNumber(),
                passwordEncoder.encode(request.pinCode()));

        String sessionId = UUID.randomUUID().toString();
        String otp = generateOtp();

        emailSender.sendOtpEmail(email, otp);

        redis.opsForValue().set(sessionKey(sessionId), toJson(pending), 15, TimeUnit.MINUTES);
        redis.opsForValue().set(otpKey(sessionId), otp, 3, TimeUnit.MINUTES);
        redis.opsForValue().set(cooldownKey(sessionId), "1", 1, TimeUnit.MINUTES);

        log.info("Registration session created: {}", sessionId);
        return new RegisterResponse(sessionId, "OTP kodu e-poçt ünvanınıza göndərildi");
    }

    @Transactional
    public void verifyAndRegisterUser(VerifyRequest request) {
        String sessionId = request.signUpSessionId();

        PendingRegistration pending = readPending(sessionId);

        String cachedOtp = redis.opsForValue().get(otpKey(sessionId));
        if (cachedOtp == null) {
            log.warn("OTP expired [session {}]", sessionId);
            throw new RuntimeException("OTP-nin vaxtı bitib");
        }

        if (!MessageDigest.isEqual(
                cachedOtp.getBytes(StandardCharsets.UTF_8),
                request.otp().getBytes(StandardCharsets.UTF_8))) {
            handleWrongOtp(sessionId);
        }

        assertNotRegistered(pending.email(), pending.mobileNumber());

        RoleEntity role = roleRepository.findByName(DEFAULT_ROLE)
                .orElseThrow(() -> {
                    log.error("{} DB-də tapılmadı!", DEFAULT_ROLE);
                    return new RuntimeException("Rol tapılmadı: " + DEFAULT_ROLE);
                });

        UserEntity user = UserEntity.builder()
                .name(pending.name())
                .surname(pending.surname())
                .email(pending.email())
                .password(pending.passwordHash())
                .mobileNumber(pending.mobileNumber())
                .pinCode(pending.pinCode())
                .roles(new HashSet<>(Set.of(role)))
                .build();

        try {
            userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException e) {
            throw new RuntimeException("Bu email və ya mobil nömrə artıq qeydiyyatdadır", e);
        }

        clearSession(sessionId);
        log.info("User registered: {}", mask(pending.email()));

        emailSender.sendWelcomeEmail(
                pending.email(),
                "CorBank-a xoş gəldiniz",
                pending.name() + " " + pending.surname() + ", qeydiyyatınız uğurla tamamlandı!");
    }

    public void resendOtp(ResendOtpRequest request) {
        String sessionId = request.signUpSessionId();

        PendingRegistration pending = readPending(sessionId);

        if (Boolean.TRUE.equals(redis.hasKey(cooldownKey(sessionId)))) {
            log.warn("Resend blocked by cooldown [session {}]", sessionId);
            throw new RuntimeException("Yeni OTP üçün bir az gözləyin");
        }

        String otp = generateOtp();
        emailSender.sendOtpEmail(pending.email(), otp);

        redis.delete(List.of(otpKey(sessionId), attemptKey(sessionId)));
        redis.opsForValue().set(otpKey(sessionId), otp, 3, TimeUnit.MINUTES);
        redis.opsForValue().set(cooldownKey(sessionId), "1", 1, TimeUnit.MINUTES);

        log.info("New OTP sent [session {}]", sessionId);
    }

    private void handleWrongOtp(String sessionId) {
        Long attempts = redis.opsForValue().increment(attemptKey(sessionId));
        if (attempts != null && attempts == 1) {
            redis.expire(attemptKey(sessionId), 3, TimeUnit.MINUTES);
        }
        int remaining = MAX_ATTEMPTS - (attempts == null ? 1 : attempts.intValue());

        if (remaining <= 0) {
            redis.delete(List.of(otpKey(sessionId), attemptKey(sessionId)));
            log.warn("Max OTP attempts exceeded [session {}]", sessionId);
            throw new RuntimeException("Yanlış cəhd limiti aşıldı, yeni OTP istəyin");
        }
        log.warn("Invalid OTP [session {}], remaining {}", sessionId, remaining);
        throw new RuntimeException("OTP yanlışdır. Qalan cəhd: " + remaining);
    }

    private void assertNotRegistered(String email, String mobileNumber) {
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Bu email artıq qeydiyyatdadır");
        }
        if (userRepository.existsByMobileNumber(mobileNumber)) {
            throw new RuntimeException("Bu mobil nömrə artıq qeydiyyatdadır");
        }
    }

    private PendingRegistration readPending(String sessionId) {
        String json = redis.opsForValue().get(sessionKey(sessionId));
        if (json == null) {
            log.warn("Registration session expired [session {}]", sessionId);
            throw new RuntimeException("Qeydiyyat sessiyasının vaxtı bitib");
        }
        try {
            return objectMapper.readValue(json, PendingRegistration.class);
        } catch (Exception e) {
            throw new RuntimeException("Qeydiyyat sessiyası oxunmadı", e);
        }
    }

    private String toJson(PendingRegistration pending) {
        try {
            return objectMapper.writeValueAsString(pending);
        } catch (Exception e) {
            throw new IllegalStateException("Qeydiyyat sessiyası yazılmadı", e);
        }
    }

    private void clearSession(String sessionId) {
        redis.delete(List.of(sessionKey(sessionId), otpKey(sessionId),
                attemptKey(sessionId), cooldownKey(sessionId)));
    }

    private String generateOtp() {
        return String.format("%06d", RANDOM.nextInt(1_000_000));
    }

    private String normalize(String email) {
        return email.trim().toLowerCase();
    }

    private String mask(String email) {
        int at = email.indexOf('@');
        return at <= 1 ? "***" : email.charAt(0) + "***" + email.substring(at);
    }

    private String sessionKey(String id)  { return "reg:session:" + id; }
    private String otpKey(String id)      { return "reg:otp:" + id; }
    private String attemptKey(String id)  { return "reg:attempts:" + id; }
    private String cooldownKey(String id) { return "reg:cooldown:" + id; }
}