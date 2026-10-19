package az.corbank.mscorbank.adapter.out.pasha.dto.account;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PaginationDto {

    private Integer count;
    private Boolean hasNextPage;
    private Integer offset;
    private Integer total;
}