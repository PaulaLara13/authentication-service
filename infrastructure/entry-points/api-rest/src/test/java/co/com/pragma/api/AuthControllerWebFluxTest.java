package co.com.pragma.api;

import co.com.pragma.api.security.jwt.JwtTokenProvider;
import co.com.pragma.model.user.Role;
import co.com.pragma.model.user.RoleName;
import co.com.pragma.model.user.User;
import co.com.pragma.model.user.dto.JwtAuthenticationResponse;
import co.com.pragma.model.user.dto.LoginRequest;
import co.com.pragma.usecase.auth.AuthUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;

class AuthControllerWebFluxTest {

    private WebTestClient webTestClient;
    private AuthUseCase authUseCase;
    private JwtTokenProvider jwtTokenProvider;
    private AuthController authController;

    @BeforeEach
    void init() {
        authUseCase = Mockito.mock(AuthUseCase.class);
        jwtTokenProvider = Mockito.mock(JwtTokenProvider.class);
        authController = new AuthController(authUseCase, jwtTokenProvider);
        webTestClient = WebTestClient.bindToController(authController).build();
    }

    @Test
    void givenValidCredentials_whenLogin_thenReturnTokens() {
        var email = "admin@demo.com";
        var password = "secret";
        var request = new LoginRequest();
        request.setEmail(email);
        request.setPassword(password);

        Mockito.when(authUseCase.authenticateUser(any(LoginRequest.class)))
                .thenReturn(Mono.just(JwtAuthenticationResponse.builder().accessToken("validated").refreshToken("validated").build()));

        var user = User.builder()
                .email(email)
                .password(password)
                .roles(Set.of(Role.from(RoleName.ROLE_ADMIN)))
                .build();
        Mockito.when(authUseCase.getCurrentUser(eq(email))).thenReturn(Mono.just(user));

        Mockito.when(jwtTokenProvider.generateTokenForUser(eq(email), any()))
                .thenReturn("access-token");
        Mockito.when(jwtTokenProvider.generateRefreshTokenForUser(eq(email)))
                .thenReturn("refresh-token");

        webTestClient.post()
                .uri("/api/v1/login")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(request), LoginRequest.class)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.accessToken").isEqualTo("access-token")
                .jsonPath("$.refreshToken").isEqualTo("refresh-token");
    }
}
