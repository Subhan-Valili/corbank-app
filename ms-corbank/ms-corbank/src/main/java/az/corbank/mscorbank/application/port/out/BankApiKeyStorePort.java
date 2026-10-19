package az.corbank.mscorbank.application.port.out;

import az.corbank.mscorbank.domain.model.Bank;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

/** İstifadəçinin bank üzrə Open API Key-lərinin saxlanması (açıq mətn kimi yox, şifrələnmiş). */
public interface BankApiKeyStorePort {

    /** Hər bank üçün açarı yazır; bu bank üçün köhnə açar varsa üzərinə yazır. Hamısı bir tranzaksiyada. */
    void saveAll(String email, Map<Bank, String> apiKeysByBank);

    /** Şifrəsi açılmış açar; bank qoşulmayıbsa boş. */
    Optional<String> find(String email, Bank bank);

    Set<Bank> connectedBanks(String email);

    /** @return açar silindisə true, ümumiyyətlə qoşulu deyildisə false */
    boolean delete(String email, Bank bank);
}
