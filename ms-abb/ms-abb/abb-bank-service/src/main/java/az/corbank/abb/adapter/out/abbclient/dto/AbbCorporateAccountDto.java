package az.corbank.abb.adapter.out.abbclient.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;

/**
 * GET /payments/corporate-account-info — spec §4.21. The spec's own example JSON is
 * malformed (mismatched braces / duplicated fields across two objects); this record
 * lists the union of fields that appear across the example, all optional in practice.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AbbCorporateAccountDto(
        String name,
        Branch accountBranch,
        BigDecimal availableBalance,
        String currency,
        String accountNo,
        String iban,
        BigDecimal todayOpeningBalance,
        BigDecimal todayIncome,
        BigDecimal todayOutcome
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Branch(String branchNumber, String branchName) {
    }
}
