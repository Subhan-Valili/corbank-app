package az.ingress.mspashabank.repository;

import az.ingress.mspashabank.entity.CustomerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<CustomerEntity, Long> {

    Optional<CustomerEntity> findByCustomerNo(String customerNo);

    Optional<CustomerEntity> findByPin(String pin);

    Optional<CustomerEntity> findByTin(String tin);

    boolean existsByPin(String pin);

    boolean existsByTin(String tin);
}