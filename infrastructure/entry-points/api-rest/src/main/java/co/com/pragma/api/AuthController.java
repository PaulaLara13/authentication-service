package co.com.pragma.api;

import co.com.pragma.infrastructure.security.jwt.JwtTokenProvider;
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

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthUseCase authUseCase;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/login")
    public ResponseEntity<JwtAuthenticationResponse> login(@Validated @RequestBody LoginRequest request) {
        log.info("Iniciando autenticación para email: {}", request.getEmail());

        // Validación de credenciales vía caso de uso (dominio)
        authUseCase.authenticateUser(request);

        // En este punto las credenciales son válidas. Generamos JWT.
        var roles = authUseCase.getCurrentUser(request.getEmail())
                .map(u -> u.getRoles().stream()
                        .map(r -> r.getName().name())
                        .collect(Collectors.toList()))
                .orElse(List.of("ROLE_USER"));
        var accessToken = jwtTokenProvider.generateTokenForUser(request.getEmail(), roles);
        var refreshToken = jwtTokenProvider.generateRefreshTokenForUser(request.getEmail());

        var response = JwtAuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();

        log.info("Autenticación exitosa para email: {}", request.getEmail());
        return ResponseEntity.ok(response);
    }
}
