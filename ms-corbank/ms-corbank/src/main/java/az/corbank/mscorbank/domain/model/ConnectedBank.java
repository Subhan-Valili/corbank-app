package az.corbank.mscorbank.domain.model;

import java.util.List;

/** Uğurla qoşulmuş bank və qoşulma anında bankdan çəkilmiş hesablar (balanslarla). */
public record ConnectedBank(Bank bank, List<Account> accounts) {
}
