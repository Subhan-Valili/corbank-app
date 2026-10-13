package az.corbank.mscorbank.application.port.out;

import az.corbank.mscorbank.domain.model.Account;
import az.corbank.mscorbank.domain.model.Bank;
import az.corbank.mscorbank.domain.model.Operation;
import az.corbank.mscorbank.domain.model.PaymentCommand;
import az.corbank.mscorbank.domain.model.PaymentResult;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Everything the application needs from ONE bank. One adapter per bank; the application
 * discovers them all through Spring and never refers to a concrete bank client.
 * <p>
 * Read methods throw {@link BankUnavailableException} on technical failure so that the
 * application decides how to degrade. {@link #submitPayment} instead reports a REJECTED
 * result, because a payment must always give the caller an answer.
 */
public interface BankGateway {

    Bank bank();

    /** Yalnız bu PIN-in sahibi olan müştərinin hesabları. */
    List<Account> findAllAccounts(String pin);

    List<Operation> findOperations(String accountId, LocalDate from, LocalDate to);

    /** Cheap "latest operations across accounts" view. Banks that cannot provide it return an empty list. */
    default List<Operation> findRecentOperations(String pin, int limit) {
        return List.of();
    }

    /** Bu PIN-lə bu bankda müştəri varmı (qeydiyyat yoxlaması). PIN ilə axtarışı olmayan banklar false qaytarır. */
    default boolean customerExists(String pin) {
        return false;
    }

    PaymentResult submitPayment(PaymentCommand command);
}
