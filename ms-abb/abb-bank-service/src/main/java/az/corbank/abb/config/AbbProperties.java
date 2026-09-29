package az.corbank.abb.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Maps the "abb.api.*" section of application.yml. Values come straight from the
 * "ABB Business API Integration Service — Technical specifications REST API v1.6" doc.
 *
 * base-url is the test/sandbox host from the doc (https://api-test-c2b.abb-bank.az);
 * every endpoint path below is relative to it exactly as documented (e.g. "/payments/auth/token").
 */
@ConfigurationProperties(prefix = "abb.api")
public record AbbProperties(
        String baseUrl,
        String username,
        String password,
        int connectTimeoutMs,
        int readTimeoutMs
) {
}
