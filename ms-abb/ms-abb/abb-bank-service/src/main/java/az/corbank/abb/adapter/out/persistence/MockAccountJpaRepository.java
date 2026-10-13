package az.corbank.abb.adapter.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MockAccountJpaRepository extends JpaRepository<MockAccountJpaEntity, String> {
    List<MockAccountJpaEntity> findByPinCode(String pinCode);

    List<MockAccountJpaEntity> findByMobileNumber(String mobileNumber);

    List<MockAccountJpaEntity> findByPinCodeAndMobileNumber(String pinCode, String mobileNumber);
}
