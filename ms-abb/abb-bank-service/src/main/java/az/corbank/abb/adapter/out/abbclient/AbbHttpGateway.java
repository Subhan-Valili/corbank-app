package az.corbank.abb.adapter.out.abbclient;

import az.corbank.abb.adapter.out.abbclient.dto.*;
import az.corbank.abb.application.port.out.AbbBankGateway;
import az.corbank.abb.domain.exception.AbbGatewayException;
import az.corbank.abb.domain.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Implements {@link AbbBankGateway} against the real ABB Business API (B2B Integration
 * REST API v1.6). Every method name in the interface maps 1:1 to a section of that spec
 * (§4.1 - §4.22); see the per-method comments below for exactly which.
 *
 * This class — and the raw *.dto records next to it — are the ONLY place in the service
 * that knows what ABB's JSON actually looks like. Everything above the AbbBankGateway
 * port (application layer, domain layer, web adapter) only ever sees domain types.
 */
@Slf4j
@Component
class AbbHttpGateway implements AbbBankGateway {

    private final RestClient restClient;
    private final AbbTokenService tokenService;

    AbbHttpGateway(RestClient abbRestClient, AbbTokenService tokenService) {
        this.restClient = abbRestClient;
        this.tokenService = tokenService;
    }

    // ---- accounts (§4.9, §4.10, §4.21) --------------------------------------------------

    @Override
    public List<CorporateAccount> listAccounts() {
        List<AbbCorporateAccountDto> raw = withAuth(token -> restClient.get()
                .uri("/payments/corporate-account-info")
                .headers(h -> h.setBearerAuth(token))
                .retrieve()
                .body(new ParameterizedTypeReference<List<AbbCorporateAccountDto>>() {
                }));
        return raw.stream().map(this::toCorporateAccount).toList();
    }

    @Override
    public AccountBalance getBalance(String accountNumber) {
        AbbBalanceResponse raw = withAuth(token -> restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/payments/account/balance")
                        .queryParam("accountNumber", accountNumber)
                        .build())
                .headers(h -> h.setBearerAuth(token))
                .retrieve()
                .body(AbbBalanceResponse.class));
        return new AccountBalance(raw.accountNumber(), raw.currency(), raw.availableBalance(),
                java.time.Instant.now(), DataOrigin.LIVE);
    }

    @Override
    public AccountStatement getStatement(StatementQuery query) {
        AbbStatementResponse raw = withAuth(token -> restClient.get()
                .uri(uriBuilder -> {
                    var b = uriBuilder.path("/payments/account/statement")
                            .queryParam("account", query.accountNumber())
                            .queryParam("from-date", query.fromDate())
                            .queryParam("to-date", query.toDate())
                            .queryParam("page-size", query.pageSize())
                            .queryParam("page", query.page());
                    String opType = toAbbOperationType(query.operationType());
                    if (opType != null) {
                        b = b.queryParam("operation-type", opType);
                    }
                    return b.build();
                })
                .headers(h -> h.setBearerAuth(token))
                .retrieve()
                .body(AbbStatementResponse.class));

        List<StatementLine> lines = raw.transaction().transactions().stream()
                .map(this::toStatementLine)
                .toList();

        return AccountStatement.of(
                raw.accountInfo() != null ? raw.accountInfo().accountNumber() : query.accountNumber(),
                raw.accountInfo() != null ? raw.accountInfo().currency() : null,
                raw.transaction().openingBalance(),
                raw.transaction().closingBalance(),
                raw.transaction().currentPage(),
                raw.transaction().pageCount(),
                raw.transaction().itemsCount(),
                lines);
    }

    // ---- payments (§4.2-4.5, §4.7, §4.8, §4.11-4.13) ------------------------------------

    @Override
    public String submitPayment(PaymentSubmission submission) {
        AbbPaymentSubmitRequest body = new AbbPaymentSubmitRequest(submission.base64aDoc(), submission.externalReference());
        String path = switch (submission.type()) {
            case REGULAR -> "/payments/";
            case SIGNED -> "/payments/signed";
            case OTP -> "/payments/otp";
            case SALARY -> "/payments/salary";
            case SALARY_SIGNED -> "/payments/salary/signed";
        };
        AbbPaymentSubmitResponse response = withAuth(token -> restClient.post()
                .uri(path)
                .headers(h -> h.setBearerAuth(token))
                .body(body)
                .retrieve()
                .body(AbbPaymentSubmitResponse.class));
        return response.data().batchNumber();
    }

    @Override
    public String verifyOtp(String batchNumber, String otpCode) {
        AbbVerifyOtpResponse response = withAuth(token -> restClient.post()
                .uri("/payments/verify-otp")
                .headers(h -> h.setBearerAuth(token))
                .body(new AbbVerifyOtpRequest(batchNumber, otpCode))
                .retrieve()
                .body(AbbVerifyOtpResponse.class));
        return response.status();
    }

    @Override
    public BatchStatusResult getBatchStatus(String batchNumber, boolean isSalaryBatch) {
        String path = isSalaryBatch ? "/payments/salary/{batchNumber}" : "/payments/{batchNumber}";
        AbbBatchStatusResponse raw = withAuth(token -> restClient.get()
                .uri(path, batchNumber)
                .headers(h -> h.setBearerAuth(token))
                .retrieve()
                .body(AbbBatchStatusResponse.class));

        List<PaymentLine> payments = raw.payments() == null ? List.of() : raw.payments().stream()
                .map(this::toPaymentLine)
                .toList();

        return new BatchStatusResult(toBatchStatusCode(raw.status().status()), raw.status().description(), payments);
    }

    @Override
    public PaymentLine getIndividualPayment(String batchNumber, String paymentId) {
        // spec §4.8 — the URL example shows paymentId as a path segment, but the parameter
        // table classifies it as a query param; we trust the structured table (see README).
        AbbBatchStatusResponse.PaymentItem raw = withAuth(token -> restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/payments/{batchNumber}")
                        .queryParam("paymentId", paymentId)
                        .build(batchNumber))
                .headers(h -> h.setBearerAuth(token))
                .retrieve()
                .body(AbbBatchStatusResponse.PaymentItem.class));
        return toPaymentLine(raw);
    }

    @Override
    public FileStatus getFileStatus(String externalReference) {
        AbbFileStatusResponse raw = withAuth(token -> restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/payments/filestatus")
                        .queryParam("external-reference", externalReference)
                        .build())
                .headers(h -> h.setBearerAuth(token))
                .retrieve()
                .body(AbbFileStatusResponse.class));
        return new FileStatus(raw.externalReference(), raw.batchNumber(),
                raw.status() != null ? toBatchStatusCode(raw.status().status()) : null,
                raw.status() != null ? raw.status().description() : null);
    }

    // ---- reference data (§4.14-4.18 — no Authorization header per spec) ----------------

    @Override
    public List<BudgetType> getBudgetTypes() {
        List<AbbBudgetTypeDto> raw = unauthenticated(() -> restClient.get()
                .uri("/payments/budget-type")
                .retrieve()
                .body(new ParameterizedTypeReference<List<AbbBudgetTypeDto>>() {
                }));
        return raw.stream().map(d -> new BudgetType(d.bdgtype(), d.bdgtypeName())).toList();
    }

    @Override
    public List<BudgetCode> getBudgetCodes() {
        List<AbbBudgetCodeDto> raw = unauthenticated(() -> restClient.get()
                .uri("/payments/budget-code")
                .retrieve()
                .body(new ParameterizedTypeReference<List<AbbBudgetCodeDto>>() {
                }));
        return raw.stream().map(d -> new BudgetCode(d.bdgcode(), d.bdgcodeName())).toList();
    }

    @Override
    public List<BankCode> getBankCodes() {
        List<AbbBankCodeDto> raw = unauthenticated(() -> restClient.get()
                .uri("/payments/bank/param")
                .retrieve()
                .body(new ParameterizedTypeReference<List<AbbBankCodeDto>>() {
                }));
        return raw.stream().map(d -> new BankCode(d.bankCode(), d.bankName(), d.swiftAddr())).toList();
    }

    @Override
    public Page<ForeignBankCode> getForeignBankCodes(int pageNumber, int pageSize) {
        AbbForeignBankCodePage raw = unauthenticated(() -> restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/payments/foreign-bank/param")
                        .queryParam("page-number", pageNumber)
                        .queryParam("page-size", pageSize)
                        .build())
                .retrieve()
                .body(AbbForeignBankCodePage.class));
        List<ForeignBankCode> items = raw.items().stream()
                .map(i -> new ForeignBankCode(i.bicCode(), i.bankName()))
                .toList();
        return new Page<>(items, raw.currentPage(), raw.pageCount(), raw.pageSize(), raw.itemsCount());
    }

    @Override
    public List<CurrencyRate> getCurrencyRates() {
        List<AbbCurrencyRateDto> raw = unauthenticated(() -> restClient.get()
                .uri("/payments/currency-rate")
                .retrieve()
                .body(new ParameterizedTypeReference<List<AbbCurrencyRateDto>>() {
                }));
        return raw.stream()
                .map(d -> new CurrencyRate(d.rateType(), d.branchCode(), d.ccy1(), d.midRate(), d.buyRate(), d.saleRate()))
                .toList();
    }

    // ---- swift & debit advice (§4.19, §4.20, §4.22) -------------------------------------

    @Override
    public List<SwiftTrackingEntry> getSwiftTracking(String referenceId) {
        List<AbbSwiftTrackDto> raw = withAuth(token -> restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/payments/swift-track")
                        .queryParam("referenceId", referenceId)
                        .build())
                .headers(h -> h.setBearerAuth(token))
                .retrieve()
                .body(new ParameterizedTypeReference<List<AbbSwiftTrackDto>>() {
                }));
        return raw.stream().map(d -> new SwiftTrackingEntry(
                d.id(), d.contractRefNo(), d.processDate(), d.track(), d.bicCode(), d.bicName(),
                d.status(), d.statusDetail(), d.amount(), d.currency(), d.amountCharge(), d.currencyCharge(), d.exchangeRate()
        )).toList();
    }

    @Override
    public void sendSwiftFile(String accountNumber, String refNumber, String fileName, byte[] zipBytes) {
        withAuth(token -> {
            MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
            parts.add("accountNumber", accountNumber);
            if (refNumber != null) {
                parts.add("refNumber", refNumber);
            }
            parts.add("file", new ByteArrayResource(zipBytes) {
                @Override
                public String getFilename() {
                    return fileName;
                }
            });
            return restClient.post()
                    .uri("/payments/swift-files/send")
                    .headers(h -> {
                        h.setBearerAuth(token);
                        h.setContentType(MediaType.MULTIPART_FORM_DATA);
                    })
                    .body(parts)
                    .retrieve()
                    .body(AbbSwiftFileSendResponse.class);
        });
    }

    @Override
    public byte[] getDebitAdviceByRrn(String rrn) {
        return withAuth(token -> restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/payments/debit-advice")
                        .queryParam("rrn", rrn)
                        .build())
                .headers(h -> {
                    h.setBearerAuth(token);
                    h.setAccept(List.of(MediaType.APPLICATION_PDF, MediaType.APPLICATION_OCTET_STREAM));
                })
                .retrieve()
                .body(byte[].class));
    }

    // ---- mapping: raw ABB DTO -> domain model -------------------------------------------

    private CorporateAccount toCorporateAccount(AbbCorporateAccountDto d) {
        return new CorporateAccount(d.name(), d.currency(), d.accountNo(), d.iban(), d.availableBalance());
    }

    private StatementLine toStatementLine(AbbStatementResponse.StatementLine line) {
        boolean isCredit = "C".equalsIgnoreCase(line.drCr())
                || (line.crAmount() != null && line.crAmount().signum() > 0);
        BigDecimal amount = (isCredit ? line.crAmount() : line.drAmount());
        amount = amount != null ? amount.abs() : BigDecimal.ZERO;
        return new StatementLine(line.trnRef(), line.trnDate(), line.trnDesc(), line.counterParty(),
                line.beneficiaryTin(), amount, isCredit ? Direction.IN : Direction.OUT);
    }

    private PaymentLine toPaymentLine(AbbBatchStatusResponse.PaymentItem item) {
        return new PaymentLine(item.paymentId(), item.transactionReference(),
                item.paymentStatus() != null ? item.paymentStatus().status() : null,
                item.paymentAmount(), item.recipientAccount(), item.paymentTime());
    }

    private BatchStatusCode toBatchStatusCode(String abbStatus) {
        if (abbStatus == null) return BatchStatusCode.ERROR;
        try {
            return BatchStatusCode.valueOf(abbStatus.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("Unrecognized ABB file status '{}', mapping to ERROR", abbStatus);
            return BatchStatusCode.ERROR;
        }
    }

    private String toAbbOperationType(StatementQuery.OperationType type) {
        if (type == null) return null;
        return switch (type) {
            case ALL -> "A";
            case DEBIT -> "D";
            case CREDIT -> "C";
        };
    }

    // ---- plumbing ------------------------------------------------------------------------

    /** Runs an authenticated call; on a 401, refreshes the token once and retries. */
    private <T> T withAuth(Function<String, T> call) {
        String token = tokenService.getAccessToken();
        try {
            return call.apply(token);
        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == 401) {
                log.debug("ABB call got 401 with cached token, forcing refresh and retrying once");
                String freshToken = tokenService.forceRefresh();
                try {
                    return call.apply(freshToken);
                } catch (RestClientResponseException retryEx) {
                    throw toGatewayException(retryEx);
                }
            }
            throw toGatewayException(ex);
        }
    }

    /** Runs a call to one of the 5 reference-data endpoints the spec shows without an Authorization header. */
    private <T> T unauthenticated(Supplier<T> call) {
        try {
            return call.get();
        } catch (RestClientResponseException ex) {
            throw toGatewayException(ex);
        }
    }

    private AbbGatewayException toGatewayException(RestClientResponseException ex) {
        String body = ex.getResponseBodyAsString();
        log.warn("ABB API call failed: {} {}", ex.getStatusCode(), body);
        return new AbbGatewayException("ABB API call failed: " + ex.getStatusCode(), ex);
    }
}
