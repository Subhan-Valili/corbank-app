package az.corbank.mscorbank.adapter.out.pasha.dto.bulk;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {
    private String type;
    private BigDecimal amount;
    private String commissionAccount;
    private String description;
    private Boolean fileRequired;


    private PayeeDto payee;

    private PayerDto payer;

    private String referenceNumber;
    private Boolean urgent;
}
