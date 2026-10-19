package az.corbank.mscorbank.domain.model;

/** "Bank qoş" ekranında bir sətir: bank, API Key ilə qoşula bilirmi, bu istifadəçi üçün qoşulubmu. */
public record BankStatus(Bank bank, boolean apiKeySupported, boolean connected) {
}
