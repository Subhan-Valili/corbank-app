package az.corbank.abb.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MockOperationJpaRepository extends JpaRepository<MockOperationJpaEntity, Long> {

    List<MockOperationJpaEntity> findByAccountNumberOrderByIdDesc(String accountNumber);
}
