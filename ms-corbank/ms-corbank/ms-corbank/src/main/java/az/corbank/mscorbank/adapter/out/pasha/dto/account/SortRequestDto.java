package az.corbank.mscorbank.adapter.out.pasha.dto.account;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SortRequestDto {

    /**
     * "ASC" | "DESC"
     */
    private String order;

    /**
     * "date", "amount", "type", "source", "description", "id"
     */
    private String orderBy;
}