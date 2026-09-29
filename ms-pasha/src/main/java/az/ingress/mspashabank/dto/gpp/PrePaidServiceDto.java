package az.ingress.mspashabank.dto.gpp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrePaidServiceDto {
    private Integer code;
    private String description;
}
