package az.corbank.mscorbank.adapter.in.web.dto;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

@Builder
public record ProjectDto(
        String id,
        String type,     // "credit" | "deposit" | "payroll"
        String name,
        BigDecimal amount,
        String currency,
        String meta,
        List<DetailRow> details
) {
    public record DetailRow(String label, String value) {
    }
}
