package co.com.pragma.infrastructure.security.jwt;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class JwtTokenProviderTest {

    @Test
    void givenProvider_whenInit_thenNoException() {
        var provider = new JwtTokenProvider();
        assertNotNull(provider);
        assertDoesNotThrow(provider::init);
    }
}
