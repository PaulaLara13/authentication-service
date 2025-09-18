package co.com.pragma.api.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.cors.reactive.CorsWebFilter;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class CorsConfigTest {

    private CorsConfig corsConfig;
    private CorsWebFilter corsWebFilter;

    @BeforeEach
    void setUp() {
        corsConfig = new CorsConfig();
        corsWebFilter = corsConfig.corsWebFilter("http://example.com,http://another.com");
    }

    @Test
    void givenPreflightRequest_whenFilter_thenCompletes() {
        var request = MockServerHttpRequest
                .method(HttpMethod.OPTIONS, "/api/v1/user")
                .header("Origin", "http://example.com")
                .header("Access-Control-Request-Method", "GET")
                .header("Access-Control-Request-Headers", "Content-Type")
                .build();
        var exchange = MockServerWebExchange.from(request);

        assertNotNull(corsWebFilter);
        // Should not throw; completion means filter handled the exchange
        corsWebFilter.filter(exchange, ex -> reactor.core.publisher.Mono.empty()).block();

        ServerHttpResponse response = exchange.getResponse();
        assertNotNull(response);
        assertNotNull(response.getStatusCode());
    }
}