package az.ingress.mspashabank.service.impl;

import az.ingress.mspashabank.dto.account.PashaAccountResponseDto;
import az.ingress.mspashabank.entity.PashaAccountEntity;
import az.ingress.mspashabank.mapper.PashaAccountMapper;
import az.ingress.mspashabank.repository.PashaBankAccountRepository;
import az.ingress.mspashabank.service.PashaBankAccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PashaBankAccountServiceImpl implements PashaBankAccountService {

    private final PashaBankAccountRepository pashaBankAccountRepository;
    private final PashaAccountMapper pashaAccountMapper;

    @Override
    @Transactional(readOnly = true)
    public List<PashaAccountResponseDto> getAccounts(String customerNo) {
        log.info("ActionLog.getAccounts.start - customerNo: {}", customerNo);

        List<PashaAccountEntity> entities =
                pashaBankAccountRepository.findAccountsByCustomerNo(customerNo);

        log.info("ActionLog.getAccounts.end - customerNo: {}, count: {}", customerNo, entities.size());
        return pashaAccountMapper.toDto(entities);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PashaAccountResponseDto> getAccountsByPin(String pin) {
        log.info("ActionLog.getAccountsByPin.start - pin: {}", maskPin(pin));

        List<PashaAccountEntity> entities =
                pashaBankAccountRepository.findAccountsByCustomerPin(pin);

        log.info("ActionLog.getAccountsByPin.end - pin: {}, count: {}", maskPin(pin), entities.size());
        return pashaAccountMapper.toDto(entities);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PashaAccountResponseDto> getAccountsByTin(String tin) {
        log.info("ActionLog.getAccountsByTin.start - tin: {}", tin);

        List<PashaAccountEntity> entities =
                pashaBankAccountRepository.findAccountsByCustomerTin(tin);

        log.info("ActionLog.getAccountsByTin.end - tin: {}, count: {}", tin, entities.size());
        return pashaAccountMapper.toDto(entities);
    }

    @Override
    @Transactional(readOnly = true)
    public PashaAccountResponseDto getAccountByAccountId(String accountId) {
        log.info("ActionLog.getAccountByAccountId.start - accountId: {}", accountId);

        PashaAccountEntity account = pashaBankAccountRepository.findAccountByAccountId(accountId)
                .orElseThrow(() -> {
                    log.error("ActionLog.getAccountByAccountId.error - account not found: {}", accountId);
                    return new RuntimeException("Account not found with accountId: " + accountId);
                });

        log.info("ActionLog.getAccountByAccountId.end - accountId: {}", accountId);
        return pashaAccountMapper.toDto(account);
    }

    @Override
    @Transactional(readOnly = true)
    public PashaAccountResponseDto getAccountByIban(String iban) {
        log.info("ActionLog.getAccountByIban.start - iban: {}", iban);

        PashaAccountEntity account = pashaBankAccountRepository.findAccountByIban(iban)
                .orElseThrow(() -> {
                    log.error("ActionLog.getAccountByIban.error - account not found: {}", iban);
                    return new RuntimeException("Account not found with iban: " + iban);
                });

        log.info("ActionLog.getAccountByIban.end - iban: {}", iban);
        return pashaAccountMapper.toDto(account);
    }

    private static String maskPin(String pin) {
        if (pin == null || pin.length() < 3) {
            return "***";
        }
        return "****" + pin.substring(pin.length() - 3);
    }
}