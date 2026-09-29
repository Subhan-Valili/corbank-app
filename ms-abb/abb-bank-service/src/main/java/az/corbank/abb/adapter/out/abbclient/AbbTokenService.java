package az.corbank.abb.adapter.out.abbclient;

import az.corbank.abb.adapter.out.abbclient.dto.AbbTokenRequest;
import az.corbank.abb.adapter.out.abbclient.dto.AbbTokenResponse;
import az.corbank.abb.config.AbbProperties;
import az.corbank.abb.domain.exception.AbbGatewayException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Infrastructure concern: obtains and caches ABB's bearer token (POST /payments/auth/token,
 * spec §4.1). Lives in the outbound adapter, not the domain/application layer, since "how
 * we authenticate to ABB" is purely a detail of this particular adapter implementation.
 */
@Slf4j
@Component
class AbbTokenService {

    private final RestClient restClient;
    private final AbbProperties props;
    private final AtomicReference<CachedToken> cache = new AtomicReference<>();

    AbbTokenService(RestClient abbRestClient, AbbProperties props) {
        this.restClient = abbRestClient;
        this.props = props;
    }

    synchronized String getAccessToken() {
        CachedToken current = cache.get();
        if (current != null && current.isValid()) {
            return current.accessToken();
        }
        return fetchNewToken();
    }

    /** Forces a refresh, used when a downstream call comes back 401 despite a "valid" cached token. */
    synchronized String forceRefresh() {
        cache.set(null);
        return fetchNewToken();
    }

    private String fetchNewToken() {
        try {
            AbbTokenResponse response = restClient.post()
                    .uri("/payments/auth/token")
                    .body(new AbbTokenRequest(props.username(), props.password()))
                    .retrieve()
                    .body(AbbTokenResponse.class);

            if (response == null || response.accessToken() == null) {
                throw new AbbGatewayException("ABB auth/token returned an empty response");
            }

            // refresh 60s early so we never hand out a token that expires mid-request
            Instant expiresAt = Instant.now().plusSeconds(Math.max(response.expiresIn() - 60, 30));
            cache.set(new CachedToken(response.accessToken(), expiresAt));
            log.debug("Fetched new ABB access token, valid until {}", expiresAt);
            return response.accessToken();
        } catch (RestClientResponseException ex) {
            log.warn("ABB auth/token failed: {} {}", ex.getStatusCode(), ex.getResponseBodyAsString());
            throw new AbbGatewayException("ABB auth/token failed: " + ex.getStatusCode(), ex);
        }
    }

    private record CachedToken(String accessToken, Instant expiresAt) {
        boolean isValid() {
            return Instant.now().isBefore(expiresAt);
        }
    }
}
