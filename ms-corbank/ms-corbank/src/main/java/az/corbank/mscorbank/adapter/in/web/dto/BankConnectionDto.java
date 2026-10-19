package az.corbank.mscorbank.adapter.in.web.dto;

import lombok.Builder;

/** Qoşulmuş (hesabı olan) real bank: dashboard-da "Digər banklar" bölməsində keçid üçün. */
@Builder
public record BankConnectionDto(
        String code,          // "PASHA" | "ABB"
        String name,          // göstərilən ad
        int accountCount
) {
}
