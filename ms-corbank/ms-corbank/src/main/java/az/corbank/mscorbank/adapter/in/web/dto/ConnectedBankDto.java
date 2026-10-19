package az.corbank.mscorbank.adapter.in.web.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record ConnectedBankDto(String code, String name, int accountCount, List<WebAccountDto> accounts) {
}
