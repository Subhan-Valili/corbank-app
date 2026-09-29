package az.corbank.mscorbank.adapter.out.pasha.dto.account;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OperationFilterDto {

    /**
     * NOT: Hazırda AccountOperationEntity-də bu sahəyə qarşılıq yoxdur.
     * Servis səviyyəsində qəbul olunur, lakin filter kimi tətbiq edilmir (yalnız loglanır).
     */
    private String activation;

    private AmountRangeDto amountValue;
    private DateRangeDto date;
    private String searchValue;
}