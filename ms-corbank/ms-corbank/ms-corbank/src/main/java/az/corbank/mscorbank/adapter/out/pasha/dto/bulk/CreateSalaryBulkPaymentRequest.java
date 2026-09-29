package az.corbank.mscorbank.adapter.out.pasha.dto.bulk;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateSalaryBulkPaymentRequest {
    String bulkDescription;
    List payments;
}