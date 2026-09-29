package az.ingress.mspashabank.repository;

import az.ingress.mspashabank.entity.B2bBulkPaymentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface B2bBulkPaymentRepository extends JpaRepository<B2bBulkPaymentEntity, Long> {

    @Query("""
            select distinct b from B2bBulkPaymentEntity b
            left join fetch b.payments
            where b.bulkId = :bulkId
            """)
    Optional<B2bBulkPaymentEntity> findByBulkId(@Param("bulkId") Long bulkId);

    @Query("""
            select distinct b from B2bBulkPaymentEntity b
            left join fetch b.payments p
            where p.referenceNumber = :referenceNumber
            """)
    Optional<B2bBulkPaymentEntity> findByPayments_ReferenceNumber(@Param("referenceNumber") String referenceNumber);
}