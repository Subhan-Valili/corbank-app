package az.corbank.mscorbank.application.port.out;

public interface CustomerRegistryPort {

    /**
     * Bu PIN ilə inteqrasiya olunmuş bankların ən azı birində müştəri varmı.
     *
     * @throws BankUnavailableException cavab tapılmayıb və bank(lar)dan biri əlçatmaz olduğu üçün
     *                                  "yoxdur" demək mümkün deyil
     */
    boolean existsByPin(String pin);
}
