package az.corbank.mscorbank.adapter.in.web.dto;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record WebOperationDto(
        String id,
        String date,          // yyyy-MM-dd (Fmt.dateAz üçün)
        String counterparty,
        String description,
        BigDecimal amount,
        String currency,
        String direction,     // "in" | "out"
        String status         // "completed" | "pending" | "rejected"
) {
}
