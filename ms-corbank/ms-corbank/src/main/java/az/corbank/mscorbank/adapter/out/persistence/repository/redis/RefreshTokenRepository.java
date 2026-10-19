package az.corbank.mscorbank.adapter.out.persistence.repository.redis;

import az.corbank.mscorbank.adapter.out.persistence.entity.RefreshToken;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends CrudRepository<RefreshToken, String> {

    Optional<RefreshToken> findByEmail(String email);
}