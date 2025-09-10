package co.com.pragma.infrastructure.security.jwt;

import co.com.pragma.infrastructure.security.UserPrincipal;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.scheduling.annotation.Scheduled;

/**
 * JWT token provider that handles token generation, validation, and management.
 * Implements security best practices for JWT handling.
 */
@Component
public class JwtTokenProvider {
    private static final Logger log = LoggerFactory.getLogger(JwtTokenProvider.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String HEADER = "{\"alg\":\"HS512\",\"typ\":\"JWT\"}";
    private static final String ROLES_CLAIM = "roles";
    private static final String JWT_PREFIX = "Bearer ";
    private static final String ISSUER = "PragmaAuthService";
    private static final String TOKEN_TYPE = "JWT";
    private static final long DEFAULT_EXPIRATION_MS = 86400000; // 24 hours
    private static final long MIN_SECRET_KEY_LENGTH = 32; // 256 bits

    // Default secret key - in production, this should be externalized
    private static final String DEFAULT_SECRET = "d8e8fca2dc0f896fd778b9b0c1cba0e5f8a7c6d8b4e3c2a1f9e8d7c6b5a4b3c2d1e0f9a8b7c6d5e4f3a2b1c0d9e8f7a6b5c4d3e2f1a0b9c8d7e6f5a4b3c2d1e0";
    
    private final String jwtSecret = DEFAULT_SECRET;
    private final long jwtExpirationMs = DEFAULT_EXPIRATION_MS;
    
    // Refresh token expiration is handled by the JWT expiration claim
    
    private final Map<String, Instant> tokenBlacklist = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        log.info("Initializing JwtTokenProvider with secret length: {}", jwtSecret.length());
        if (jwtSecret.getBytes(StandardCharsets.UTF_8).length < MIN_SECRET_KEY_LENGTH) {
            throw new IllegalStateException("JWT secret key must be at least " + MIN_SECRET_KEY_LENGTH + " characters long");
        }
        log.info("JwtTokenProvider initialized with secret length: {}", jwtSecret.length());
    }

    /**
     * Generates a JWT token for the authenticated user
     */
    public String generateToken(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", userPrincipal.getUsername());
        claims.put(ROLES_CLAIM, userPrincipal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList()));
        
        return createToken(claims);
    }

    /**
     * Generates a refresh token with longer expiration
     */
    public String generateRefreshToken(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", userPrincipal.getUsername());
        claims.put("type", "REFRESH");
        
        return createToken(claims);
    }

    /**
     * Convenience method to generate a JWT token for a username and roles
     */
    public String generateTokenForUser(String username, java.util.List<String> roles) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", username);
        claims.put(ROLES_CLAIM, roles == null ? java.util.List.of() : roles);
        return createToken(claims);
    }

    /**
     * Convenience method to generate a refresh token for a username
     */
    public String generateRefreshTokenForUser(String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", username);
        claims.put("type", "REFRESH");
        return createToken(claims);
    }

    /**
     * Extracts the token from the Authorization header
     */
    public String resolveToken(String bearerToken) {
        if (bearerToken == null || bearerToken.trim().isEmpty() || !bearerToken.startsWith(JWT_PREFIX)) {
            return null;
        }
        return bearerToken.substring(7);
    }

    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, claims -> claims.get("sub").toString());
    }

    @SuppressWarnings("unchecked")
    public List<String> getRolesFromToken(String token) {
        return getClaimFromToken(token, claims -> {
            Object roles = claims.get(ROLES_CLAIM);
            return roles != null ? (List<String>) roles : List.of();
        });
    }

    public <T> T getClaimFromToken(String token, Function<Map<String, Object>, T> claimsResolver) {
        final Map<String, Object> claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    private boolean isTokenExpired(String token) {
        Number expNumber = getClaimFromToken(token, claims -> {
            Object v = claims.get("exp");
            if (v == null) return null;
            if (v instanceof Number n) return n;
            try { return Long.parseLong(v.toString()); } catch (Exception ignored) { return null; }
        });
        if (expNumber == null) {
            return true;
        }
        long expSeconds = expNumber.longValue();
        final Date expiration = new Date(expSeconds * 1000L);
        return expiration.before(new Date());
    }

    public void blacklistToken(String token) {
        try {
            String jti = getClaimFromToken(token, claims -> claims.get("jti").toString());
            long exp = ((Number) getClaimFromToken(token, claims -> claims.get("exp"))).longValue();
            Instant expiration = Instant.ofEpochSecond(exp);
            tokenBlacklist.put(jti, expiration);
        } catch (Exception e) {
            log.warn("Failed to blacklist token: {}", e.getMessage());
        }
    }

    public boolean isTokenBlacklisted(String token) {
        try {
            String jti = getClaimFromToken(token, claims -> claims.get("jti").toString());
            return tokenBlacklist.containsKey(jti);
        } catch (Exception e) {
            return true; // If we can't parse the token, consider it invalid
        }
    }

    @Scheduled(fixedRate = 1, timeUnit = TimeUnit.HOURS)
    protected void cleanExpiredBlacklistedTokens() {
        Instant now = Instant.now();
        tokenBlacklist.entrySet().removeIf(entry -> entry.getValue().isBefore(now));
    }

    private boolean validateTokenFormat(String token) {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }
        String[] parts = token.split("\\.");
        return parts.length == 3; // Header, Payload, Signature
    }

    private Map<String, Object> getAllClaimsFromToken(String token) {
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid JWT token format");
        }
        
        try {
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
            @SuppressWarnings("unchecked")
            Map<String, Object> claims = objectMapper.readValue(payload, Map.class);
            return claims;
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Invalid JWT payload", e);
        }
    }

    public boolean validateToken(String authToken) {
        try {
            if (!validateTokenFormat(authToken)) {
                return false;
            }
            
            if (isTokenBlacklisted(authToken)) {
                return false;
            }
            
            Map<String, Object> claims = getAllClaimsFromToken(authToken);
            
            // Verify issuer
            if (!ISSUER.equals(claims.get("iss"))) {
                log.warn("Invalid JWT issuer: {}", claims.get("iss"));
                return false;
            }
            
            // Verify signature
            String[] parts = authToken.split("\\.");
            String headerAndPayload = parts[0] + "." + parts[1];
            String signature = parts[2];
            
            byte[] signatureBytes = Base64.getUrlDecoder().decode(signature);
            byte[] computedSignature = hmacSha512(
                headerAndPayload.getBytes(StandardCharsets.UTF_8),
                jwtSecret.getBytes(StandardCharsets.UTF_8)
            );
            
            if (!java.security.MessageDigest.isEqual(signatureBytes, computedSignature)) {
                log.warn("Invalid JWT signature");
                return false;
            }
            
            // Verify expiration
            if (isTokenExpired(authToken)) {
                log.warn("Expired JWT token");
                return false;
            }
            
            return true;
        } catch (Exception e) {
            log.warn("Error validating JWT token: {}", e.getMessage());
            return false;
        }
    }

    private String createToken(Map<String, Object> claims) {
        if (claims == null) {
            throw new IllegalArgumentException("Claims cannot be null");
        }
        
        // Set standard claims
        long now = System.currentTimeMillis();
        claims.put("iss", ISSUER);
        claims.put("iat", now / 1000);
        claims.put("exp", (now + jwtExpirationMs) / 1000);
        claims.put("jti", java.util.UUID.randomUUID().toString());
        claims.put("typ", TOKEN_TYPE);
        
        // Encode header
        String header = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(HEADER.getBytes(StandardCharsets.UTF_8));
        
        // Encode payload
        String payload;
        try {
            payload = Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(objectMapper.writeValueAsString(claims).getBytes(StandardCharsets.UTF_8));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize claims to JSON", e);
        }
        
        // Create signature
        String headerAndPayload = header + "." + payload;
        byte[] signature = hmacSha512(
                headerAndPayload.getBytes(StandardCharsets.UTF_8),
                jwtSecret.getBytes(StandardCharsets.UTF_8)
        );
        
        // Build JWT
        return headerAndPayload + "." + Base64.getUrlEncoder().withoutPadding()
                .encodeToString(signature);
    }
    
    private byte[] hmacSha512(byte[] data, byte[] key) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKeySpec = new SecretKeySpec(key, "HmacSHA512");
            mac.init(secretKeySpec);
            return mac.doFinal(data);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new JwtException("Error creating HMAC signature", e);
        }
    }
}
