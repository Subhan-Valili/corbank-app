package az.ingress.mspashabank.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "gpp_invoices")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GppInvoiceEntity extends BaseEntity {

    String externalInvoiceId; // JSON-dakı "id"
    String invoiceCode;
    String invoiceType;
    String stateCode;
    String receiptNumber;
    String feeCalculationMethod;

    BigDecimal amountDue;
    BigDecimal commissionAmount;
    BigDecimal currentDebt;
    BigDecimal maxAllowedAmount;
    BigDecimal minAllowedAmount;

    Boolean partialPaymentAllowed;
    Boolean manualPaymentReceiverSelectionRequired;
    Boolean manualSpBranchSelectionRequired;

    Integer serviceCode;
    String serviceDescription;
    Integer spBranchCode;
    String spBranchDescription;

    Integer paymentReceiverCode;
    String paymentReceiverDescription;

    @ElementCollection
    @CollectionTable(name = "gpp_invoice_payment_receivers", joinColumns = @JoinColumn(name = "invoice_id"))
    @Builder.Default
    List<PaymentReceiverEmbeddable> paymentReceivers = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id")
    GppPaymentEntity payment;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    List<GppChildInvoiceEntity> childInvoices = new ArrayList<>();

    public void addChildInvoice(GppChildInvoiceEntity childInvoice) {
        childInvoices.add(childInvoice);
        childInvoice.setInvoice(this);
    }

    @Embeddable
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentReceiverEmbeddable {
        Integer code;
        String description;
    }
}