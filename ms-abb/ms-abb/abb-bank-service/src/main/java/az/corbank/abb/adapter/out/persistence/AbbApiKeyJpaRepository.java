package az.corbank.abb.adapter.out.persistence;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AbbApiKeyJpaRepository extends JpaRepository<AbbApiKeyJpaEntity, Long> {
    Optional<AbbApiKeyJpaEntity> findByKeyHashAndActiveTrue(String keyHash);
    boolean existsByKeyHash(String keyHash);
}
