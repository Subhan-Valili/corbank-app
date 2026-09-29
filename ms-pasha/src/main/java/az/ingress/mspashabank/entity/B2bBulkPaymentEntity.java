package az.ingress.mspashabank.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "b2b_bulk_payments")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class B2bBulkPaymentEntity extends BaseEntity {

    @Column(name = "bulk_id")
    Long bulkId;

    @Column(name = "bulk_description")
    String bulkDescription;

    @Column(name = "record_count")
    Integer recordCount;

    @Column(name = "status")
    String status;

    @OneToMany(mappedBy = "bulkPayment", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    List<B2bPaymentItemEntity> payments = new ArrayList<>();

    public void addPayment(B2bPaymentItemEntity payment) {
        payments.add(payment);
        payment.setBulkPayment(this);
    }
}