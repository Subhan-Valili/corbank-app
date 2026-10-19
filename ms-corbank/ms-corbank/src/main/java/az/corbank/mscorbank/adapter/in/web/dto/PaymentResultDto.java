package az.corbank.mscorbank.adapter.in.web.dto;

import lombok.Builder;

@Builder
public record PaymentResultDto(
        String status,   // "completed" | "rejected"
        String message,
        String source,   // "live" (ms-pasha-ya getdi) | "mock"
        Long bulkId
) {
}
