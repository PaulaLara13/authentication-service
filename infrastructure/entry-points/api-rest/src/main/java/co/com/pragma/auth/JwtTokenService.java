package co.com.pragma.auth;

import co.com.pragma.api.dto.TokenResponse;
import co.com.pragma.model.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class JwtTokenService {
    private final JwtEncoder jwtEncoder;
    private final long ttlSeconds;
    private final String issuer;


    public Mono<TokenResponse> generate(User user) {
        var now = java.time.Instant.now();
        var claims = org.springframework.security.oauth2.jwt.JwtClaimsSet.builder()
                .issuer(issuer)
                .issuedAt(now)
                .expiresAt(now.plusSeconds(ttlSeconds))
                .subject(user.getMail())
                .claim("uid", user.getId())
                .claim("email", user.getMail())
                .claim("role", user.getRole().getName()) // ADMIN/ADVISOR/CLIENT desde BD
                .build();
        var header = org.springframework.security.oauth2.jwt.JwsHeader.with(org.springframework.security.oauth2.jose.jws.MacAlgorithm.HS256).build();
        var jwt = jwtEncoder.encode(org.springframework.security.oauth2.jwt.JwtEncoderParameters.from(header, claims));
        return Mono.just(new TokenResponse(jwt.getTokenValue(), "Bearer", ttlSeconds));
    }
}
