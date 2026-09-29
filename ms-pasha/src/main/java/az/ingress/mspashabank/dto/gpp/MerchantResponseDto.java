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
public class MerchantResponseDto {
    private Integer code;
    private String displayName;
    private List<MerchantIdentifierDto> identifiers;
    private String name;
}
