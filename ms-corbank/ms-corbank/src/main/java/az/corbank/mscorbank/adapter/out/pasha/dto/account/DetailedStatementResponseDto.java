package az.corbank.mscorbank.adapter.out.pasha.dto.account;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DetailedStatementResponseDto {

    List<StatementOperationDto> content;
    int pageNumber;
    int pageSize;
    long totalElements;
    int totalPages;
}