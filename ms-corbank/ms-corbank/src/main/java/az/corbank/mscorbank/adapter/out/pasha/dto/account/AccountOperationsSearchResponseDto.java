package az.corbank.mscorbank.adapter.out.pasha.dto.account;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AccountOperationsSearchResponseDto {

    private List<AccountOperationDto> data;
    private PaginationDto pagination;
}