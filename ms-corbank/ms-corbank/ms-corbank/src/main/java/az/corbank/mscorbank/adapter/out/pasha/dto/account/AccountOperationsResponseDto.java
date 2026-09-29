package az.corbank.mscorbank.adapter.out.pasha.dto.account;

import java.util.List;

public record AccountOperationsResponseDto(
        List<AccountOperationDto> data
) {
}
