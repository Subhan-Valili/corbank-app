package az.ingress.mspashabank.dto.gpp;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PayerDto {
    String accountNumber;
    String tin;
    String type;
}