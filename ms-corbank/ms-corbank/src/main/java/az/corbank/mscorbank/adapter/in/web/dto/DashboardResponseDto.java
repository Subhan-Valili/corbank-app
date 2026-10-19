package az.corbank.mscorbank.adapter.in.web.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record DashboardResponseDto(
        List<WebAccountDto> accounts,
        List<ProjectDto> otherProjects,
        List<WebOperationDto> recentOperations,
        List<BankBalanceDto> otherBanks,
        List<FxRateViewDto> exchangeRates,
        List<BankConnectionDto> connectedBanks,   // hesabı olan real banklar (PASHA, ABB) — keçid üçün
        String selectedBank                       // hazırda seçilmiş bank kodu, seçim yoxdursa null (hamısı)
) {
}
