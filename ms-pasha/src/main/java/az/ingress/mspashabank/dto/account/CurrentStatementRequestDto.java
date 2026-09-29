package az.ingress.mspashabank.dto.account;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import java.time.LocalDate;

@Builder
public record CurrentStatementRequestDto(
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate fromDate,
        Integer pageNumber,
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate toDate
) {}