package az.corbank.mscorbank.application.port.out;

import az.corbank.mscorbank.domain.model.BankBalance;

import java.util.List;

/** Balances held at banks that are not integrated (yet). */
public interface OtherBanksPort {

    List<BankBalance> findOtherBanks();
}
