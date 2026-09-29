package az.ingress.mspashabank.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Entity
@Table(name = "gpp_child_invoices")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GppChildInvoiceEntity extends BaseEntity {

    String externalChildInvoiceId;
    String invoiceCode;
    String receiptNumber;
    String feeCalculationMethod;

    BigDecimal amount;
    BigDecimal commissionAmount;

    Integer serviceCode;
    String serviceDescription;

    Integer paymentReceiverCode;
    String paymentReceiverDescription;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id")
    GppInvoiceEntity invoice;
}