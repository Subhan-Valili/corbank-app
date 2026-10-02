package az.corbank.abb.adapter.out.abbclient;

import az.corbank.abb.adapter.out.abbclient.dto.AbbCorporateAccountDto;
import az.corbank.abb.adapter.out.abbclient.dto.AbbPaymentSubmitRequest;
import az.corbank.abb.adapter.out.abbclient.dto.AbbPaymentSubmitResponse;
import az.corbank.abb.adapter.out.abbclient.dto.AbbStatementResponse;
import az.corbank.abb.application.port.out.AbbBankGateway;
import az.corbank.abb.domain.exception.AbbGatewayException;
import az.corbank.abb.domain.model.AccountStatement;
import az.corbank.abb.domain.model.CorporateAccount;
import az.corbank.abb.domain.model.Direction;
import az.corbank.abb.domain.model.PaymentInstruction;
import az.corbank.abb.domain.model.StatementLine;
import az.corbank.abb.domain.model.StatementQuery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Function;

/**
 * Implements {@link AbbBankGateway} against the real ABB Business API (B2B Integration
 * REST API v1.6) — trimmed to exactly the 2 endpoints "Əsas səhifə" needs:
 *   - GET /payments/corporate-account-info   (spec §4.21 — account list)
 *   - GET /payments/account/statement        (spec §4.10 — operations)
 *
 * This class — and the raw *.dto records next to it — are the ONLY place in the service
 * that knows what ABB's JSON actually looks like. Everything above the AbbBankGateway
 * port (application layer, domain layer, web adapter) only ever sees domain types.
 *
 * The other 20 methods of the spec (payments, OTP, reference data, SWIFT, debit advice)
 * were implemented once and removed since nothing calls them yet — see AbbBankGateway's
 * javadoc for why, and how to bring a given one back when it's actually needed.
 *
 * Active whenever "abb.api.mock-enabled" is NOT "true" — see AbbMockGateway for the other
 * half of this pair, used while there's no real network access to ABB yet.
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "abb.api", name = "mock-enabled", havingValue = "false", matchIfMissing = true)
class AbbHttpGateway implements AbbBankGateway {

    private final RestClient restClient;
    private final AbbTokenService tokenService;

    AbbHttpGateway(RestClient abbRestClient, AbbTokenService tokenService) {
        this.restClient = abbRestClient;
        this.tokenService = tokenService;
    }

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

        String accountNumber = raw.accountInfo() != null ? raw.accountInfo().accountNumber() : query.accountNumber();
        String currency = raw.accountInfo() != null ? raw.accountInfo().currency() : null;
        List<StatementLine> lines = raw.transaction().transactions().stream()
                .map(line -> toStatementLine(line, accountNumber, currency))
                .toList();

        return AccountStatement.of(
                accountNumber,
                currency,
                raw.transaction().openingBalance(),
                raw.transaction().closingBalance(),
                raw.transaction().currentPage(),
                raw.transaction().pageCount(),
                raw.transaction().itemsCount(),
                lines);
    }

    @Override
    public String submitPayment(PaymentInstruction instruction) {
        String xml = AbbPaymentOrderXmlBuilder.build(instruction);
        String base64aDoc = java.util.Base64.getEncoder().encodeToString(xml.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        String externalReference = java.util.UUID.randomUUID().toString();

        AbbPaymentSubmitResponse response = withAuth(token -> restClient.post()
                .uri("/payments/")
                .headers(h -> h.setBearerAuth(token))
                .body(new AbbPaymentSubmitRequest(base64aDoc, externalReference))
                .retrieve()
                .body(AbbPaymentSubmitResponse.class));

        if (response == null || response.data() == null || response.data().batchNumber() == null) {
            throw new AbbGatewayException("ABB payments/ returned no batchNumber");
        }
        return response.data().batchNumber();
    }

    // ---- mapping: raw ABB DTO -> domain model -------------------------------------------

    private CorporateAccount toCorporateAccount(AbbCorporateAccountDto d) {
        return new CorporateAccount(d.name(), d.currency(), d.accountNo(), d.iban(), d.availableBalance());
    }

    private StatementLine toStatementLine(AbbStatementResponse.StatementLine line, String accountNumber, String currency) {
        boolean isCredit = "C".equalsIgnoreCase(line.drCr())
                || (line.crAmount() != null && line.crAmount().signum() > 0);
        BigDecimal amount = (isCredit ? line.crAmount() : line.drAmount());
        amount = amount != null ? amount.abs() : BigDecimal.ZERO;
        return new StatementLine(accountNumber, line.trnRef(), line.trnDate(), line.trnDesc(), line.counterParty(),
                line.beneficiaryTin(), amount, currency, isCredit ? Direction.IN : Direction.OUT);
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

    /**
     * Runs an authenticated call; on a 401, refreshes the token once and retries.
     * Catches RestClientException broadly — not just RestClientResponseException (ABB
     * responded with an error status) but also e.g. ResourceAccessException (connection
     * reset, DNS failure, TLS handshake aborted, timeout — no HTTP response at all). Both
     * are equally "the gateway failed" from the caller's point of view.
     */
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
                } catch (RestClientException retryEx) {
                    throw toGatewayException(retryEx);
                }
            }
            throw toGatewayException(ex);
        } catch (RestClientException ex) {
            throw toGatewayException(ex);
        }
    }

    private AbbGatewayException toGatewayException(RestClientException ex) {
        if (ex instanceof RestClientResponseException responseEx) {
            log.warn("ABB API call failed: {} {}", responseEx.getStatusCode(), responseEx.getResponseBodyAsString());
            return new AbbGatewayException("ABB API call failed: " + responseEx.getStatusCode(), ex);
        }
        log.warn("ABB API unreachable: {}", ex.getMessage());
        return new AbbGatewayException("ABB API unreachable: " + ex.getMessage(), ex);
    }
}
