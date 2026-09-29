package az.corbank.mscorbank.adapter.out.pasha.dto.account;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class SortDto {
    String direction; // "ASC" və ya "DESC"
    String field;     // "DATE", "AMOUNT" və s.
}