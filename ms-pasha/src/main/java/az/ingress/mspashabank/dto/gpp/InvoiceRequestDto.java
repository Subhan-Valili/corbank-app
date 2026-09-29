package az.ingress.mspashabank.dto.gpp;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InvoiceRequestDto {
    String additionalCode;
    String code;
    String identificationSubtype;
    String prefix;
    Integer scCode;
    List<Integer> serviceCodeList;
    Integer spCode;
}