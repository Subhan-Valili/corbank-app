package az.corbank.abb.application.port.out;

import az.corbank.abb.domain.model.AccountStatement;
import az.corbank.abb.domain.model.CorporateAccount;
import az.corbank.abb.domain.model.PaymentInstruction;
import az.corbank.abb.domain.model.StatementQuery;

import java.util.List;

/**
 * Driven port: everything the application layer needs from ABB Bank, in pure domain
 * vocabulary. This is the seam between "what we need from a bank" (owned by this module)
 * and "how ABB Bank happens to expose it today" (owned by whichever adapter implements
 * this — see adapter.out.abbclient.AbbHttpGateway). If ABB changed their entire API
 * tomorrow, or we swapped in a mock for testing, nothing above this interface would change.
 *
 * Trimmed to exactly what "Əsas səhifə" needs: the account list (spec §4.21) and a
 * statement per account (spec §4.10). Payments, OTP, reference data, SWIFT and debit
 * advice (spec §4.1-4.8, §4.11-4.22) were fully implemented at one point but removed —
 * nothing in the current UI calls them, and dead code serving no screen just adds
 * surface area to review and keep in sync with the spec for no benefit yet. They're
 * straightforward to bring back the same way (domain model + port method + gateway
 * implementation + controller) once a real feature needs them.
 */
public interface AbbBankGateway {

    List<CorporateAccount> listAccounts();

    AccountStatement getStatement(StatementQuery query);

    /**
     * Submits a payment (spec §4.3 for AbbHttpGateway; simulated directly against Postgres
     * for AbbMockGateway). Returns ABB's batchNumber (or the mock equivalent) on success;
     * throws AbbGatewayException if the bank/mock rejects or can't be reached. Balance
     * sufficiency is checked by the caller (PaymentApplicationService) beforehand — this
     * method assumes that's already been done.
     */
    String submitPayment(PaymentInstruction instruction);
}
