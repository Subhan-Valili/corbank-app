package az.ingress.mspashabank.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "gpp_payments")
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GppPaymentEntity extends BaseEntity {

    Long externalPaymentId; // Bank tərəfindən qayıdan unikal id
    String description;
    String status;
    String type;
    String bankStatusCode;

    @Column(length = 1000)
    String rejectReason;

    BigDecimal transferAmount;
    BigDecimal commissionAmount;
    LocalDateTime createDate; // Bank sistemindəki yaradılma tarixi

    // Payer
    String payerAccountNumber;
    String payerTin;
    String payerType;

    // InvoiceRequest
    String invoiceRequestCode;
    String invoiceRequestPrefix;
    String invoiceRequestAdditionalCode;
    String invoiceRequestIdentificationSubtype;
    Integer invoiceRequestScCode;
    Integer invoiceRequestSpCode;

    @ElementCollection
    @CollectionTable(name = "gpp_payment_service_codes", joinColumns = @JoinColumn(name = "payment_id"))
    @Column(name = "service_code")
    @Builder.Default
    List<Integer> invoiceRequestServiceCodeList = new ArrayList<>();

    @OneToMany(mappedBy = "payment", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    List<GppInvoiceEntity> invoices = new ArrayList<>();

    public void addInvoice(GppInvoiceEntity invoice) {
        invoices.add(invoice);
        invoice.setPayment(this);
    }
}