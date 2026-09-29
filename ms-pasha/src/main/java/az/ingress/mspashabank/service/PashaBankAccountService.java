package az.ingress.mspashabank.service;

import az.ingress.mspashabank.dto.account.PashaAccountResponseDto;

import java.util.List;

public interface PashaBankAccountService {
    List<PashaAccountResponseDto> getAccounts(String customerNo);
    PashaAccountResponseDto getAccountByAccountId(String accountId);
    PashaAccountResponseDto getAccountByIban(String iban);
}
