package az.corbank.mscorbank.adapter.out.pasha.dto.gpp;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PaymentReceiverDto {
    Integer code;
    String description;
}