package co.com.pragma.api;

import co.com.pragma.api.security.jwt.JwtTokenProvider;
import co.com.pragma.model.user.dto.JwtAuthenticationResponse;
import co.com.pragma.model.user.dto.LoginRequest;
import co.com.pragma.usecase.auth.AuthUseCase;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import java.util.List;
import java.util.stream.Collectors;
import static co.com.pragma.common.Constants.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    private final AuthUseCase authUseCase;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/login")
    public Mono<ResponseEntity<JwtAuthenticationResponse>> login(@Validated @RequestBody LoginRequest request) {
        log.info(INICIANDO_AUTENTICACIÓN_EMAIL, request.getEmail());
        return authUseCase.authenticateUser(request)
                .doOnNext(r -> log.info(AUTENTICACIÓN_OK_EMAIL, request.getEmail()))
                .then(authUseCase.getCurrentUser(request.getEmail())
                        .map(u -> u.getRoles().stream()
                                .map(r -> r.getName().name())
                                .collect(Collectors.toList()))
                        .defaultIfEmpty(List.of(ROLE_USER)))
                .map(roles -> {
                    log.info(ROLES_RESUELTOS_EMAIL_ROLES, request.getEmail(), roles);
                    var accessToken = jwtTokenProvider.generateTokenForUser(request.getEmail(), roles);
                    var refreshToken = jwtTokenProvider.generateRefreshTokenForUser(request.getEmail());
                    log.info(TOKENS_GENERADOS_EMAIL, request.getEmail());
                    return JwtAuthenticationResponse.builder()
                            .accessToken(accessToken)
                            .refreshToken(refreshToken)
                            .build();
                })
                .map(ResponseEntity::ok);
    }
}
