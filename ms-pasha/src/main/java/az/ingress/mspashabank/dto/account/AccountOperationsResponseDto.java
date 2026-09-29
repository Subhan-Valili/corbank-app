package az.ingress.mspashabank.dto.account;

import java.util.List;

public record AccountOperationsResponseDto(
        List<AccountOperationDto> data
) {
}
