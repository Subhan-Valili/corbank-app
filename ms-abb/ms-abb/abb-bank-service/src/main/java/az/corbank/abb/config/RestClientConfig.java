package az.corbank.abb.config;

import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.util.Timeout;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    /**
     * Plain RestClient (synchronous, Spring MVC-friendly) pointed at ABB Bank's base URL.
     * Timeouts come from abb.api.connect-timeout-ms / read-timeout-ms.
     */
    @Bean
    public RestClient abbRestClient(AbbProperties props) {
        var requestConfig = RequestConfig.custom()
                .setConnectTimeout(Timeout.of(java.time.Duration.ofMillis(props.connectTimeoutMs())))
                .setResponseTimeout(Timeout.of(java.time.Duration.ofMillis(props.readTimeoutMs())))
                .build();

        var httpClient = HttpClientBuilder.create()
                .setDefaultRequestConfig(requestConfig)
                .build();

        var requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);

        return RestClient.builder()
                .baseUrl(props.baseUrl())
                .requestFactory(requestFactory)
                .build();
    }
}