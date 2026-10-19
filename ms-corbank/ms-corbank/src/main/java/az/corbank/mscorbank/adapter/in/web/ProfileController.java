package az.corbank.mscorbank.adapter.in.web;

import az.corbank.mscorbank.adapter.out.persistence.repository.jpa.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** Login olmuş istifadəçinin öz profili (yuxarı paneldə ad/avatar üçün). PIN qaytarılmır. */
@RestController
@RequestMapping("/api/me")
@RequiredArgsConstructor
public class ProfileController {

    private final UserRepository userRepository;

    @GetMapping
    public ResponseEntity<Map<String, String>> me(Authentication auth) {
        return userRepository.findByEmail(auth.getName())
                .map(u -> ResponseEntity.ok(Map.of(
                        "name", u.getName(),
                        "surname", u.getSurname(),
                        "email", u.getEmail(),
                        "mobileNumber", u.getMobileNumber())))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
