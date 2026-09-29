package az.corbank.abb.application.port.out;

import az.corbank.abb.domain.model.*;

import java.util.List;

/**
 * Driven port: everything the application layer needs from ABB Bank, in pure domain
 * vocabulary. This is the seam between "what we need from a bank" (owned by this module)
 * and "how ABB Bank happens to expose it today" (owned by whichever adapter implements
 * this — see adapter.out.abbclient.AbbHttpGateway). If ABB changed their entire API
 * tomorrow, or we swapped in a mock for testing, nothing above this interface would change.
 *
 * Every method may throw {@link az.corbank.abb.domain.exception.AbbGatewayException} —
 * network failure, timeout, or ABB rejecting the call all surface the same way; the
 * application layer doesn't need to know which.
 */
public interface AbbBankGateway {

    List<CorporateAccount> listAccounts();

    AccountBalance getBalance(String accountNumber);

    AccountStatement getStatement(StatementQuery query);

    /** Submits via the endpoint matching submission.type() (spec §4.2-4.4, §4.11-4.12); returns the batch number ABB assigned. */
    String submitPayment(PaymentSubmission submission);

    /** Returns ABB's raw status text — spec §4.5. */
    String verifyOtp(String batchNumber, String otpCode);

    /** spec §4.7 (regular) / §4.13 (salary) — isSalaryBatch picks which endpoint to call. */
    BatchStatusResult getBatchStatus(String batchNumber, boolean isSalaryBatch);

    PaymentLine getIndividualPayment(String batchNumber, String paymentId);

    FileStatus getFileStatus(String externalReference);

    List<BudgetType> getBudgetTypes();

    List<BudgetCode> getBudgetCodes();

    List<BankCode> getBankCodes();

    Page<ForeignBankCode> getForeignBankCodes(int pageNumber, int pageSize);

    List<CurrencyRate> getCurrencyRates();

    List<SwiftTrackingEntry> getSwiftTracking(String referenceId);

    void sendSwiftFile(String accountNumber, String refNumber, String fileName, byte[] zipBytes);

    byte[] getDebitAdviceByRrn(String rrn);

    /** Result of a batch status poll — just enough to let PaymentBatch.applyRemoteStatus() update itself. */
    record BatchStatusResult(BatchStatusCode status, String statusDescription, List<PaymentLine> payments) {
    }
}
