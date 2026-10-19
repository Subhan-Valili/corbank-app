package az.corbank.mscorbank.adapter.out.pasha.dto.bulk;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SalaryPaymentItemDto {
    BigDecimal amount;
    String commissionAccount;
    String description;
    PayeeDto payee;
    PayerDto payer;
    String referenceNumber;
    String salaryType;
    String type;
}