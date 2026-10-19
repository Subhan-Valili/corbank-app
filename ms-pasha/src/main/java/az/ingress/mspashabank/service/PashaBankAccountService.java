package az.ingress.mspashabank.service;

import az.ingress.mspashabank.dto.account.PashaAccountResponseDto;

import java.util.List;

public interface PashaBankAccountService {

    List<PashaAccountResponseDto> getAccounts(String customerNo);

    List<PashaAccountResponseDto> getAccountsByPin(String pin);

    List<PashaAccountResponseDto> getAccountsByTin(String tin);

    PashaAccountResponseDto getAccountByAccountId(String accountId);

    PashaAccountResponseDto getAccountByIban(String iban);

    List<PashaAccountResponseDto> getAccountsByApiKey(String apiKey, String accountType);
}