package az.corbank.mscorbank.adapter.in.web.dto;

import lombok.Builder;

@Builder
public record BankStatusDto(String code, String name, boolean apiKeySupported, boolean connected) {
}
