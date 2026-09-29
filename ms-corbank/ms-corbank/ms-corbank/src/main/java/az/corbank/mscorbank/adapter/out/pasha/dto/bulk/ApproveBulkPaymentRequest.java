package az.corbank.mscorbank.adapter.out.pasha.dto.bulk;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApproveBulkPaymentRequest {

    private Long bulkId;

    private List<String> referenceNumbers;
}