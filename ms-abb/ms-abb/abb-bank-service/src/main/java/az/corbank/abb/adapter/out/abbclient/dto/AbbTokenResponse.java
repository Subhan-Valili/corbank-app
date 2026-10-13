package az.corbank.abb.adapter.out.abbclient.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Response of POST /payments/auth/token, per spec §4.1. Shape matches a standard
 * Keycloak/OAuth2 token response (access_token, expires_in, refresh_token, ...).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AbbTokenResponse(
        @JsonProperty("access_token") String accessToken,
        @JsonProperty("expires_in") long expiresIn,
        @JsonProperty("refresh_expires_in") long refreshExpiresIn,
        @JsonProperty("refresh_token") String refreshToken,
        @JsonProperty("token_type") String tokenType
) {
}
