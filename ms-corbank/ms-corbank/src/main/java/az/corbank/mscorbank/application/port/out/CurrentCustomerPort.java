package az.corbank.mscorbank.application.port.out;

import az.corbank.mscorbank.domain.model.Bank;

import java.util.Optional;

/** Hazırda sorğu göndərən (login olmuş) müştərinin bank PIN-i. Guest üçün boşdur. */
public interface CurrentCustomerPort {

    Optional<String> currentPin();

    /** Login olmuş istifadəçinin emaili (JWT-dən). Guest üçün boşdur. */
    Optional<String> currentEmail();

    /** Cari istifadəçinin bu bank üçün saxlanmış (şifrəsi açılmış) Open API Key-i; qoşulmayıbsa boşdur. */
    Optional<String> currentApiKey(Bank bank);
}
