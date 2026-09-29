package az.ingress.mspashabank.service.impl;

import az.ingress.mspashabank.dto.bulk.*;
import az.ingress.mspashabank.entity.AccountOperationEntity;
import az.ingress.mspashabank.entity.B2bBulkPaymentEntity;
import az.ingress.mspashabank.entity.B2bPaymentItemEntity;
import az.ingress.mspashabank.entity.PashaAccountEntity;
import az.ingress.mspashabank.enums.OperationSource;
import az.ingress.mspashabank.enums.OperationStatus;
import az.ingress.mspashabank.enums.OperationType;
import az.ingress.mspashabank.mapper.B2bPaymentMapper;
import az.ingress.mspashabank.repository.AccountOperationRepository;
import az.ingress.mspashabank.repository.B2bBulkPaymentRepository;
import az.ingress.mspashabank.repository.PashaBankAccountRepository;
import az.ingress.mspashabank.service.B2bBulkPaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class B2bBulkPaymentServiceImpl implements B2bBulkPaymentService {

    private static final String STATUS_APPROVED = "APPROVED";

    private static final Map<String, BigDecimal> AZN_MID_RATE = Map.of(
            "USD", new BigDecimal("1.7025"),
            "EUR", new BigDecimal("1.8340")
    );

    private final B2bBulkPaymentRepository bulkPaymentRepository;
    private final B2bPaymentMapper paymentMapper;
    private final PashaBankAccountRepository pashaBankAccountRepository;
    private final AccountOperationRepository accountOperationRepository;

    @Override
    @Transactional
    public CreateBulkPaymentResponse createBulkPayments(CreateBulkPaymentRequest request) {
        log.info("Processing bulk payment request with description: '{}'", request.bulkDescription());

        B2bBulkPaymentEntity bulkEntity = paymentMapper.toBulkEntity(request);

        bulkEntity.setBulkId(System.currentTimeMillis() + new Random().nextInt(1000));
        bulkEntity.setRecordCount(request.payments() != null ? request.payments().size() : 0);

        if (request.payments() != null) {
            Map<String, PashaAccountEntity> accountsByIdentifier = loadAccountsForPayments(request.payments());

            request.payments().forEach(paymentRequest -> {
                B2bPaymentItemEntity item = paymentMapper.toItemEntity(paymentRequest);
                bulkEntity.addPayment(item);

                applyLedgerEffects(paymentRequest, accountsByIdentifier);
            });
        }

        B2bBulkPaymentEntity savedEntity = bulkPaymentRepository.save(bulkEntity);

        log.info("Bulk payment successfully created with bulkId: {}", savedEntity.getBulkId());

        return paymentMapper.toCreateResponse(savedEntity);
    }


    private void applyLedgerEffects(CreateBulkPaymentRequest.PaymentRequest paymentRequest,
                                    Map<String, PashaAccountEntity> accountsByIdentifier) {
        BigDecimal amount = paymentRequest.amount();
        if (amount == null || amount.signum() <= 0) {
            log.warn("Ledger effects skipped — invalid amount: {}", amount);
            return;
        }

        String payerIdentifier = paymentRequest.payer() != null ? paymentRequest.payer().accountNumber() : null;
        String payeeIdentifier = paymentRequest.payee() != null ? paymentRequest.payee().accountNumber() : null;
        String payeeName = paymentRequest.payee() != null ? paymentRequest.payee().name() : null;

        PashaAccountEntity payerAccount = accountsByIdentifier.get(payerIdentifier);
        PashaAccountEntity payeeAccount = accountsByIdentifier.get(payeeIdentifier);

        if (payerAccount == null) {
            log.warn("Payer account not found for identifier: {} — balans/operation yaradıla bilmədi", payerIdentifier);
            return;
        }

        debit(payerAccount, amount);
        recordOperation(payerAccount, amount, payerAccount.getCurrency(),
                payeeName != null ? payeeName : (payeeAccount != null ? accountLabel(payeeAccount) : "Xarici alıcı"),
                OperationType.DEBIT, paymentRequest.description());
        pashaBankAccountRepository.save(payerAccount);

        if (payeeAccount != null) {
            BigDecimal creditAmount = convert(amount, payerAccount.getCurrency(), payeeAccount.getCurrency());
            credit(payeeAccount, creditAmount);
            recordOperation(payeeAccount, creditAmount, payeeAccount.getCurrency(),
                    accountLabel(payerAccount), OperationType.CREDIT, paymentRequest.description());
            pashaBankAccountRepository.save(payeeAccount);
        } else {
            log.info("Payee account not found for identifier '{}' — xarici/naməlum benefisiar hesab edilir, yalnız payer debet olundu.", payeeIdentifier);
        }
    }

    private Map<String, PashaAccountEntity> loadAccountsForPayments(
            List<CreateBulkPaymentRequest.PaymentRequest> payments) {

        Set<String> identifiers = payments.stream()
                .flatMap(p -> Stream.of(
                        p.payer() != null ? p.payer().accountNumber() : null,
                        p.payee() != null ? p.payee().accountNumber() : null))
                .filter(Objects::nonNull)
                .filter(id -> !id.isBlank())
                .collect(Collectors.toSet());

        if (identifiers.isEmpty()) {
            return Map.of();
        }

        List<PashaAccountEntity> accounts =
                pashaBankAccountRepository.findAccountsByAccountIdInOrIbanIn(identifiers);

        Map<String, PashaAccountEntity> accountsByIdentifier = new HashMap<>();
        for (PashaAccountEntity account : accounts) {
            if (account.getAccountId() != null) {
                accountsByIdentifier.put(account.getAccountId(), account);
            }
            if (account.getIban() != null) {
                accountsByIdentifier.put(account.getIban(), account);
            }
        }
        return accountsByIdentifier;
    }

    private String accountLabel(PashaAccountEntity account) {
        return account.getCurrency() + " hesabı · " + account.getIban();
    }

    private void debit(PashaAccountEntity account, BigDecimal amount) {
        account.setCurrentBalance(nz(account.getCurrentBalance()).subtract(amount));
        account.setAvailableBalance(nz(account.getAvailableBalance()).subtract(amount));
        account.setTodayOutcome(nz(account.getTodayOutcome()).add(amount));
    }

    private void credit(PashaAccountEntity account, BigDecimal amount) {
        account.setCurrentBalance(nz(account.getCurrentBalance()).add(amount));
        account.setAvailableBalance(nz(account.getAvailableBalance()).add(amount));
        account.setTodayIncome(nz(account.getTodayIncome()).add(amount));
    }

    private BigDecimal nz(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private void recordOperation(PashaAccountEntity account, BigDecimal amount, String currency,
                                 String counterpartyName, OperationType type, String description) {
        AccountOperationEntity operation = AccountOperationEntity.builder()
                .externalId(UUID.randomUUID().toString())
                .amountValue(amount.abs().setScale(2, RoundingMode.HALF_UP))
                .amountCurrencyCode(currency)
                .counterpartyName(counterpartyName)
                .operationDate(LocalDateTime.now())
                .description(description)
                .source(OperationSource.CURRENT)
                .type(type)
                .status(OperationStatus.COMPLETED)
                .pashaAccount(account)
                .build();
        accountOperationRepository.save(operation);
    }

    private BigDecimal convert(BigDecimal amount, String fromCcy, String toCcy) {
        if (fromCcy == null || toCcy == null || fromCcy.equalsIgnoreCase(toCcy)) {
            return amount.setScale(2, RoundingMode.HALF_UP);
        }
        BigDecimal azn = toAzn(amount, fromCcy);
        return fromAzn(azn, toCcy);
    }

    private BigDecimal toAzn(BigDecimal amount, String currency) {
        if ("AZN".equalsIgnoreCase(currency)) return amount;
        BigDecimal rate = AZN_MID_RATE.get(currency.toUpperCase());
        return rate != null ? amount.multiply(rate) : amount;
    }

    private BigDecimal fromAzn(BigDecimal aznAmount, String currency) {
        if ("AZN".equalsIgnoreCase(currency)) return aznAmount.setScale(2, RoundingMode.HALF_UP);
        BigDecimal rate = AZN_MID_RATE.get(currency.toUpperCase());
        return rate != null
                ? aznAmount.divide(rate, 2, RoundingMode.HALF_UP)
                : aznAmount.setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    @Transactional
    public void approveBulkPayments(ApproveBulkPaymentRequest request) {
        log.info("Approving bulk payment. bulkId: {}, referenceNumbers: {}",
                request.bulkId(), request.referenceNumbers());

        B2bBulkPaymentEntity bulkEntity = bulkPaymentRepository.findByBulkId(request.bulkId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Bulk payment not found for bulkId: " + request.bulkId()));

        Set<String> existingReferenceNumbers = bulkEntity.getPayments().stream()
                .map(B2bPaymentItemEntity::getReferenceNumber)
                .collect(Collectors.toSet());

        Set<String> requestedReferenceNumbers = new HashSet<>(
                request.referenceNumbers() != null ? request.referenceNumbers() : Set.of());

        if (!existingReferenceNumbers.containsAll(requestedReferenceNumbers)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "One or more referenceNumbers do not belong to bulkId: " + request.bulkId());
        }

        bulkEntity.setStatus(STATUS_APPROVED);
        bulkPaymentRepository.save(bulkEntity);

        log.info("Bulk payment with bulkId: {} successfully approved", request.bulkId());
    }

    @Override
    @Transactional(readOnly = true)
    public GetBulkIdResponse getBulkIdByReferenceNumber(String referenceNumber) {
        log.info("Looking up bulkId for referenceNumber: {}", referenceNumber);

        B2bBulkPaymentEntity bulkEntity = bulkPaymentRepository.findByPayments_ReferenceNumber(referenceNumber)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Bulk payment not found for referenceNumber: " + referenceNumber));

        return paymentMapper.toGetBulkIdResponse(bulkEntity, referenceNumber);
    }

    @Override
    @Transactional(readOnly = true)
    public FxRatesResponse getFxRates() {
        log.info("Fetching FX rates (mock data)");

        FxRatesResponse.FxRateDto usdRate = FxRatesResponse.FxRateDto.builder()
                .currency("USD")
                .targetCurrency("AZN")
                .standardBuyRate(new BigDecimal("1.7000"))
                .standardSellRate(new BigDecimal("1.7050"))
                .prefRate(FxRatesResponse.PrefRateDto.builder()
                        .buyRate(new BigDecimal("1.7020"))
                        .buyCommission(new BigDecimal("0.0010"))
                        .sellRate(new BigDecimal("1.7030"))
                        .sellCommission(new BigDecimal("0.0010"))
                        .build())
                .validUntil(Instant.now().plus(1, ChronoUnit.HOURS))
                .build();

        FxRatesResponse.FxRateDto eurRate = FxRatesResponse.FxRateDto.builder()
                .currency("EUR")
                .targetCurrency("AZN")
                .standardBuyRate(new BigDecimal("1.8300"))
                .standardSellRate(new BigDecimal("1.8380"))
                .prefRate(FxRatesResponse.PrefRateDto.builder()
                        .buyRate(new BigDecimal("1.8320"))
                        .buyCommission(new BigDecimal("0.0015"))
                        .sellRate(new BigDecimal("1.8360"))
                        .sellCommission(new BigDecimal("0.0015"))
                        .build())
                .validUntil(Instant.now().plus(1, ChronoUnit.HOURS))
                .build();

        return FxRatesResponse.builder()
                .fxRates(List.of(usdRate, eurRate))
                .isEditableRate(true)
                .build();
    }

}