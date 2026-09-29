package az.corbank.mscorbank.adapter.out.pasha.dto.bulk;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PayeeDto {
    private String accountNumber;
    private String additionalInfo;
    private String address;
    private BankDto bank;
    private String email;
    private String name;
    private String tin;
    private String type;
}
