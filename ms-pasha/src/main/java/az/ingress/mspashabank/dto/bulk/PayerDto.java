package az.ingress.mspashabank.dto.bulk;

import lombok.Builder;

@Builder
public record PayerDto(
        String accountNumber
) {}