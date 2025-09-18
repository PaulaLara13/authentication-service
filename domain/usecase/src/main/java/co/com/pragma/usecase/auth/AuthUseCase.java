package co.com.pragma.usecase.auth;

import co.com.pragma.model.user.User;
import co.com.pragma.model.user.dto.JwtAuthenticationResponse;
import co.com.pragma.model.user.dto.LoginRequest;
import co.com.pragma.model.user.gateways.AuthService;
import co.com.pragma.model.user.gateways.UserRepository;
import co.com.pragma.model.user.gateways.PasswordHasher;
import lombok.RequiredArgsConstructor;
import co.com.pragma.model.user.exception.InvalidCredentialsException;
import java.util.logging.Level;
import java.util.logging.Logger;
import reactor.core.publisher.Mono;
import java.util.Base64;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static co.com.pragma.common.Constants.*;

@RequiredArgsConstructor
public class AuthUseCase implements AuthService {
    private static final Logger logger = Logger.getLogger(AuthUseCase.class.getName());
    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;

    @Override
    public Mono<JwtAuthenticationResponse> authenticateUser(LoginRequest loginRequest) {
        logger.log(Level.INFO, STARTING_AUTHENTICATION_EMAIL, loginRequest.getEmail());
        return userRepository.findByEmail(loginRequest.getEmail())
                .switchIfEmpty(Mono.defer(() -> {
                    String errorMsg = String.format(AUTHENTICATION_FAILED, loginRequest.getEmail());
                    logger.log(Level.SEVERE, errorMsg);
                    return Mono.error(new InvalidCredentialsException(INVALID_CREDENTIALS));
                }))
                .flatMap(user -> {
                    logger.log(Level.INFO, USER_FOUND, user.getEmail());
                    if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
                        String msg = String.format(AUTHENTICATION_FAILED_INVALID, loginRequest.getEmail());
                        logger.log(Level.SEVERE, msg);
                        return Mono.error(new InvalidCredentialsException(INVALID_CREDENTIALS));
                    }
                    logger.log(Level.INFO, CHECKIING_PASSWORD, loginRequest.getEmail());
                    boolean matches = passwordHasher.matches(loginRequest.getPassword(), user.getPassword());
                    if (!matches) {
                        String msg = String.format(AUTHENTICATION_FAILED_PASSWORD, loginRequest.getEmail());
                        logger.log(Level.SEVERE, msg);
                        return Mono.error(new InvalidCredentialsException(INVALID_CREDENTIALS));
                    }
                    logger.log(Level.INFO, USER_AUTHENTICATED, loginRequest.getEmail());
                    // This use case only validates credentials. Tokens are created in the controller.
                    return Mono.just(JwtAuthenticationResponse.builder()
                            .accessToken("validated")
                            .refreshToken("validated")
                            .build());
                });
    }

    @Override
    public Mono<Boolean> validateToken(String token) {
        return Mono.fromSupplier(() -> token != null && !token.isBlank())
                .map(validStart -> {
                    if (!validStart) return false;
                    var parts = token.split("\\.");
                    return parts.length == 3 && parts[1] != null && !parts[1].isBlank();
                });
    }

    @Override
    public Mono<String> getUsernameFromToken(String token) {
        return Mono.defer(() -> {
            if (token == null || token.isBlank()) {
                return Mono.error(new IllegalArgumentException(TOKEN_IS_REQUIRED));
            }
            try {
                var parts = token.split("\\.");
                if (parts.length != 3) {
                    return Mono.error(new IllegalArgumentException(INVALID_TOKEN_FORMAT));
                }
                // Decode payload (Base64 URL) and extract "sub" claim
                byte[] payloadBytes = Base64.getUrlDecoder().decode(parts[1]);
                String payloadJson = new String(payloadBytes, StandardCharsets.UTF_8);
                Pattern p = Pattern.compile("\"sub\"\\s*:\\s*\"([^\"]+)\"");
                Matcher m = p.matcher(payloadJson);
                if (m.find()) {
                    return Mono.just(m.group(1));
                }
                return Mono.error(new IllegalArgumentException(USERNAME_SUB_CLAIM_NOT_FOUND));
            } catch (Exception e) {
                return Mono.error(new IllegalArgumentException(UNABLE_TO_PARSE_TOKEN, e));
            }
        });
    }

    public Mono<User> getCurrentUser(String email) {
        return userRepository.findByEmail(email)
                .doOnNext(u -> logger.log(Level.INFO, CURRENT_USER_RETRIEVED, email))
                .doOnError(e -> logger.log(Level.SEVERE, ERROR_RETRIEVING_CURRENT_USER, e.getMessage()));
    }
}