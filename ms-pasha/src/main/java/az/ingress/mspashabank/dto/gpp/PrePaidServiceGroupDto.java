package az.ingress.mspashabank.dto.gpp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrePaidServiceGroupDto {
    private String description;
    private List<PrePaidServiceDto> prePaidServices;
}
