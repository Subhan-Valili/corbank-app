package az.corbank.abb.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MockAccountJpaRepository extends JpaRepository<MockAccountJpaEntity, String> {
}
