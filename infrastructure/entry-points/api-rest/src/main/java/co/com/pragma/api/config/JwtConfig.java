package co.com.pragma.api.config;

import co.com.pragma.auth.JwtTokenService;
import com.nimbusds.jose.jwk.source.ImmutableSecret;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

@Configuration
public class JwtConfig {
    @Bean
    public JwtEncoder jwtEncoder(@Value("${app.jwt.secret}") String secret) {
        var key = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
        var jwkSource = new ImmutableSecret<SecretKey>(key);
        return new NimbusJwtEncoder(jwkSource);
    }
    @Bean
    public JwtTokenService tokenService(JwtEncoder encoder,
                                        @Value("${app.jwt.ttl-seconds}") long ttl,
                                        @Value("${app.jwt.issuer}") String issuer) {
        return new JwtTokenService(encoder, ttl, issuer);
    }
}
