package az.corbank.abb.adapter.out.abbclient.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.util.List;

/** GET /payments/account/statement — spec §4.10. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AbbStatementResponse(
        AccountInfo accountInfo,
        TransactionBlock transaction
) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record AccountInfo(
            String statementDate,
            Branch accountBranch,
            String customerName,
            String voen,
            String accountNumber,
            String currency
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Branch(String branchNumber, String branchName) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TransactionBlock(
            BigDecimal drSum,
            BigDecimal crSum,
            BigDecimal openingBalance,
            BigDecimal closingBalance,
            int pageCount,
            int currentPage,
            int pageSize,
            long itemsCount,
            List<StatementLine> transactions
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record StatementLine(
            String trnRef,
            BigDecimal drAmount,
            BigDecimal crAmount,
            String drCr,          // "D" (debit/məxaric) or "C" (credit/mədaxil)
            String trnDesc,
            String trnDate,       // dd.MM.yyyy, per doc examples
            String counterParty,
            String beneficiaryTin
    ) {
    }
}
