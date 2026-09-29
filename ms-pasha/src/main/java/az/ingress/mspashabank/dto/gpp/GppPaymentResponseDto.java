package az.ingress.mspashabank.dto.gpp;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class GppPaymentResponseDto {
    Long id;
    String bankStatusCode;
    BigDecimal commissionAmount;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
    LocalDateTime createDate;

    String description;
    InvoiceRequestDto invoiceRequest;
    List<InvoiceDto> invoices;
    PayerDto payer;
    String rejectReason;
    String status;
    BigDecimal transferAmount;
    String type;
}