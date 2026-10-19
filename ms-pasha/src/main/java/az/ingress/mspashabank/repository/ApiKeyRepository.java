package az.ingress.mspashabank.repository;

import az.ingress.mspashabank.entity.ApiKeyEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ApiKeyRepository extends JpaRepository<ApiKeyEntity, Long> {

    @EntityGraph(attributePaths = "customer")
    Optional<ApiKeyEntity> findByKeyHashAndActiveTrue(String keyHash);
}