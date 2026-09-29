package az.corbank.abb.application.port.in;

import az.corbank.abb.domain.model.*;

import java.util.List;

/**
 * Read-only lookups from spec §4.14-4.22. Grouped into one port rather than one
 * interface per method: unlike SubmitPaymentUseCase or GetAccountBalanceUseCase, none of
 * these carry independent business rules (no fallback, no persistence, no state
 * transitions) — they're pure pass-through queries, so splitting them further would add
 * ceremony without adding any real seam.
 */
public interface ReferenceDataUseCase {

    List<BudgetType> getBudgetTypes();

    List<BudgetCode> getBudgetCodes();

    List<BankCode> getBankCodes();

    Page<ForeignBankCode> getForeignBankCodes(int pageNumber, int pageSize);

    List<CurrencyRate> getCurrencyRates();

    List<SwiftTrackingEntry> getSwiftTracking(String referenceId);

    /** spec §4.19 — forwards a signed SWIFT ZIP file to ABB as-is. */
    void sendSwiftFile(String accountNumber, String refNumber, String fileName, byte[] zipBytes);

    /** spec §4.22 — raw PDF bytes. */
    byte[] getDebitAdviceByRrn(String rrn);
}
