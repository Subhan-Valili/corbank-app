package az.ingress.mspashabank.repository;

import az.ingress.mspashabank.entity.GppPaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GppPaymentRepository extends JpaRepository<GppPaymentEntity, Long> {

    List<GppPaymentEntity> findByPayerAccountNumber(String payerAccountNumber);

    List<GppPaymentEntity> findByStatus(String status);
}