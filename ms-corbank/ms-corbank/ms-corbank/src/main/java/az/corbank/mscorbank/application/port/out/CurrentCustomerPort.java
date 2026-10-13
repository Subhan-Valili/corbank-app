package az.corbank.mscorbank.application.port.out;

import java.util.Optional;

/** Hazırda sorğu göndərən (login olmuş) müştərinin bank PIN-i. Guest üçün boşdur. */
public interface CurrentCustomerPort {

    Optional<String> currentPin();
}
