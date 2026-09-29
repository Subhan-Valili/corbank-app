package az.corbank.mscorbank.adapter.in.web;

import az.corbank.mscorbank.domain.model.Account;
import az.corbank.mscorbank.domain.model.Bank;
import az.corbank.mscorbank.domain.model.BankBalance;
import az.corbank.mscorbank.domain.model.BankConnection;
import az.corbank.mscorbank.domain.model.Dashboard;
import az.corbank.mscorbank.domain.model.FxRate;
import az.corbank.mscorbank.domain.model.Operation;
import az.corbank.mscorbank.domain.model.OperationDirection;
import az.corbank.mscorbank.domain.model.OperationsReport;
import az.corbank.mscorbank.domain.model.PaymentCommand;
import az.corbank.mscorbank.domain.model.PaymentResult;
import az.corbank.mscorbank.domain.model.Project;
import az.corbank.mscorbank.adapter.in.web.dto.*;

import java.util.List;

final class WebMapper {

    private WebMapper() {
    }

    static WebAccountDto toDto(Account a) {
        return WebAccountDto.builder()
                .id(a.id())
                .name(a.name())
                .balance(a.balance())
                .currency(a.currency())
                .iban(a.iban())
                .status(friendlyStatus(a.status()))
                .bank(a.bank().name())
                .build();
    }

    private static String friendlyStatus(az.corbank.mscorbank.domain.model.AccountStatus status) {
        return switch (status) {
            case ACTIVE -> "Aktiv";
            case BLOCKED -> "Bloklanıb";
            case CLOSED -> "Bağlanıb";
            case OTHER -> "Naməlum";
        };
    }

    static WebOperationDto toDto(Operation op) {
        return WebOperationDto.builder()
                .id(op.id())
                .date(op.date() != null ? op.date().toString() : null)
                .counterparty(op.counterparty())
                .description(op.description())
                .amount(op.amount())
                .currency(op.currency())
                .direction(op.direction() == OperationDirection.IN ? "in" : "out")
                .status("completed")
                .build();
    }

    static OperationsResponse toDto(OperationsReport report) {
        OperationsSummaryDto summary = OperationsSummaryDto.builder()
                .income(report.summary().income())
                .expense(report.summary().expense())
                .net(report.summary().net())
                .build();
        return new OperationsResponse(report.operations().stream().map(WebMapper::toDto).toList(), summary);
    }

    static FxRateViewDto toDto(FxRate rate) {
        return FxRateViewDto.builder().currency(rate.currency()).buy(rate.buy()).sell(rate.sell()).build();
    }

    static BankBalanceDto toDto(BankBalance balance) {
        return BankBalanceDto.builder().name(balance.name()).balance(balance.balance()).currency(balance.currency()).build();
    }

    static BankConnectionDto toDto(BankConnection connection) {
        return BankConnectionDto.builder()
                .code(connection.bank().name())
                .name(connection.bank().displayName())
                .accountCount(connection.accountCount())
                .build();
    }

    static ProjectDto toDto(Project p) {
        List<ProjectDto.DetailRow> details = p.details().stream()
                .map(d -> new ProjectDto.DetailRow(d.label(), d.value()))
                .toList();
        return ProjectDto.builder()
                .id(p.id())
                .type(p.type().name().toLowerCase())
                .name(p.name())
                .amount(p.amount())
                .currency(p.currency())
                .meta(p.meta())
                .details(details)
                .build();
    }

    static DashboardResponseDto toDto(Dashboard d) {
        return DashboardResponseDto.builder()
                .accounts(d.accounts().stream().map(WebMapper::toDto).toList())
                .otherProjects(d.projects().stream().map(WebMapper::toDto).toList())
                .recentOperations(d.recentOperations().stream().map(WebMapper::toDto).toList())
                .otherBanks(d.otherBanks().stream().map(WebMapper::toDto).toList())
                .exchangeRates(d.exchangeRates().stream().map(WebMapper::toDto).toList())
                .connectedBanks(d.connectedBanks().stream().map(WebMapper::toDto).toList())
                .selectedBank(d.selectedBank() != null ? d.selectedBank().name() : null)
                .build();
    }

    static PaymentCommand toCommand(PaymentRequestDto r) {
        return new PaymentCommand(r.fromAccountId(), r.beneficiaryName(), r.beneficiaryIban(), r.amount(),
                r.currency(), r.description(), r.creditAmount(), r.creditCurrency());
    }

    static PaymentResultDto toDto(PaymentResult r) {
        return PaymentResultDto.builder()
                .status(r.status().name().toLowerCase())
                .message(r.message())
                .source(r.source().name().toLowerCase())
                .bulkId(r.bulkId())
                .build();
    }

    static Bank parseBank(String code) {
        return Bank.fromCode(code).orElse(null);
    }
}
