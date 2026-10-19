package az.corbank.mscorbank.application.port.in;

import az.corbank.mscorbank.domain.model.Bank;
import az.corbank.mscorbank.domain.model.BankStatus;
import az.corbank.mscorbank.domain.model.ConnectedBank;

import java.util.List;

/** Onboarding: istifadəçi banklarını Open API Key ilə qoşur, siyahıya baxır, ayırır. */
public interface ManageBankConnectionsUseCase {

    record ConnectCommand(Bank bank, String apiKey) {
    }

    /** Bütün dəstəklənən banklar + hansının qoşulu olduğu. */
    List<BankStatus> listBanks();

    /**
     * "Finish": bütün açarlar əvvəlcə bankdan yoxlanır (hesab/balans çəkilir); HAMISI keçərsə saxlanır,
     * biri keçməzsə heç nə saxlanmır.
     */
    List<ConnectedBank> connect(List<ConnectCommand> commands);

    void disconnect(Bank bank);
}
