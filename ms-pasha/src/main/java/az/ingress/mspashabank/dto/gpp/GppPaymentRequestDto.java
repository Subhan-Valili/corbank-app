package az.ingress.mspashabank.dto.gpp;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GppPaymentRequestDto {
    String description;
    InvoiceRequestDto invoiceRequest;
    List<InvoiceDto> invoices;
    PayerDto payer;
}