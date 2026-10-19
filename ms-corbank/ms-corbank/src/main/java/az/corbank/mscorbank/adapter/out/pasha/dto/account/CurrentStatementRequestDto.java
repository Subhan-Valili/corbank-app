package az.corbank.mscorbank.adapter.out.pasha.dto.account;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CurrentStatementRequestDto {

    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate fromDate;

    Integer pageNumber;

    @JsonFormat(pattern = "yyyy-MM-dd")
    LocalDate toDate;
}