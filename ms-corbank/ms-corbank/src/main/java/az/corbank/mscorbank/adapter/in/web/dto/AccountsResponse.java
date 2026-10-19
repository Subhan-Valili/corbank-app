package az.corbank.mscorbank.adapter.in.web.dto;

import java.util.List;

public record AccountsResponse(List<WebAccountDto> accounts) {
}
