package az.corbank.mscorbank.adapter.out.demo;

import az.corbank.mscorbank.domain.model.BankBalance;
import az.corbank.mscorbank.application.port.out.OtherBanksPort;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * Static demo balances of non-integrated banks. PASHA and ABB are deliberately absent:
 * they are real, integrated banks and show up through their BankGateway instead.
 */
@Component
class StaticOtherBanksAdapter implements OtherBanksPort {

    private static final List<BankBalance> OTHER_BANKS = List.of(
            new BankBalance("Ziraat Bank", new BigDecimal("4200"), "USD"));

    @Override
    public List<BankBalance> findOtherBanks() {
        return OTHER_BANKS;
    }
}
