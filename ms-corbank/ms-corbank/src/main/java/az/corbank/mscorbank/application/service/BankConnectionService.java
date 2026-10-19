package az.corbank.mscorbank.application.service;

import az.corbank.mscorbank.application.exception.BankConnectionException;
import az.corbank.mscorbank.application.exception.ServiceUnavailableException;
import az.corbank.mscorbank.application.port.in.ManageBankConnectionsUseCase;
import az.corbank.mscorbank.application.port.out.BankApiKeyStorePort;
import az.corbank.mscorbank.application.port.out.BankGateway;
import az.corbank.mscorbank.application.port.out.BankUnavailableException;
import az.corbank.mscorbank.application.port.out.CurrentCustomerPort;
import az.corbank.mscorbank.domain.model.Account;
import az.corbank.mscorbank.domain.model.Bank;
import az.corbank.mscorbank.domain.model.BankStatus;
import az.corbank.mscorbank.domain.model.ConnectedBank;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Slf4j
class BankConnectionService implements ManageBankConnectionsUseCase {

    private final BankGateways banks;
    private final CurrentCustomerPort customer;
    private final BankApiKeyStorePort store;

    BankConnectionService(BankGateways banks, CurrentCustomerPort customer, BankApiKeyStorePort store) {
        this.banks = banks;
        this.customer = customer;
        this.store = store;
    }

    @Override
    public List<BankStatus> listBanks() {
        Set<Bank> connected = store.connectedBanks(requireEmail());
        return Arrays.stream(Bank.values())
                .map(bank -> new BankStatus(bank, supportsApiKey(bank), connected.contains(bank)))
                .toList();
    }

    @Override
    public List<ConnectedBank> connect(List<ConnectCommand> commands) {
        String email = requireEmail();
        if (commands == null || commands.isEmpty()) {
            throw new BankConnectionException("Ən azı bir bank üçün Open API Key daxil edin");
        }

        Set<Bank> seen = EnumSet.noneOf(Bank.class);
        for (ConnectCommand c : commands) {
            if (!seen.add(c.bank())) {
                throw new BankConnectionException(c.bank().displayName() + " bir neçə dəfə göndərilib");
            }
        }

        // 1) hamısını bankdan yoxla (Feign çağırışları tranzaksiyadan kənarda)
        Map<Bank, String> keysToSave = new LinkedHashMap<>();
        List<ConnectedBank> result = new java.util.ArrayList<>();
        for (ConnectCommand c : commands) {
            BankGateway gateway = banks.require(c.bank());
            if (!gateway.supportsApiKey()) {
                throw new BankConnectionException(c.bank().displayName() + " üçün Open API Key ilə qoşulma hələ dəstəklənmir");
            }
            String apiKey = c.apiKey().trim();
            List<Account> accounts;
            try {
                accounts = gateway.findAccountsByApiKey(apiKey);   // yanlış açar -> InvalidApiKeyException
            } catch (BankUnavailableException e) {
                log.warn("{} Open API Key yoxlamasına cavab vermədi: {}", c.bank(), e.getMessage());
                throw new ServiceUnavailableException(
                        c.bank().displayName() + " hazırda əlçatmazdır, bir az sonra yenidən cəhd edin", e);
            }
            keysToSave.put(c.bank(), apiKey);
            result.add(new ConnectedBank(c.bank(), accounts));
        }

        // 2) hamısı keçdi -> saxla
        store.saveAll(email, keysToSave);
        log.info("Banks connected for user: {}", keysToSave.keySet());
        return result;
    }

    @Override
    public void disconnect(Bank bank) {
        if (!store.delete(requireEmail(), bank)) {
            throw new BankConnectionException(bank.displayName() + " qoşulu deyil");
        }
    }

    private boolean supportsApiKey(Bank bank) {
        return banks.get(bank).map(BankGateway::supportsApiKey).orElse(false);
    }

    private String requireEmail() {
        return customer.currentEmail()
                .orElseThrow(() -> new IllegalStateException("Login olmuş istifadəçi tapılmadı"));
    }
}
