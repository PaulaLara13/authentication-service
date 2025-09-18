package co.com.pragma.api.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SecurityHeadersFilterTest {

    private SecurityHeadersFilter filter;
    private MockServerWebExchange exchange;
    private WebFilterChain chain;

    @BeforeEach
    void setUp() {
        filter = new SecurityHeadersFilter();
        MockServerHttpRequest request = MockServerHttpRequest.get("/test").build();
        exchange = MockServerWebExchange.from(request);
        chain = (ex) -> Mono.empty();
    }

    @Test
    void testFilterSetsSecurityHeaders() {
        filter.filter(exchange, chain).block();

        ServerHttpResponse response = exchange.getResponse();
        assertEquals("default-src 'self'; frame-ancestors 'self'; form-action 'self'", response.getHeaders().getFirst("Content-Security-Policy"));
        assertEquals("max-age=31536000;", response.getHeaders().getFirst("Strict-Transport-Security"));
        assertEquals("nosniff", response.getHeaders().getFirst("X-Content-Type-Options"));
        assertEquals("", response.getHeaders().getFirst("Server"));
        assertEquals("no-store", response.getHeaders().getFirst("Cache-Control"));
        assertEquals("no-cache", response.getHeaders().getFirst("Pragma"));
        assertEquals("strict-origin-when-cross-origin", response.getHeaders().getFirst("Referrer-Policy"));
    }
}
