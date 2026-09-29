package az.ingress.mspashabank.dto.bulk;

import lombok.Builder;

@Builder
public record PayeeDto(
        String accountNumber,
        String additionalInfo,
        String address,
        BankDto bank,
        String email,
        String name,
        String tin,
        String type
) {

    @Builder
    public record BankDto(
            String code,
            String name
    ) {
    }
}