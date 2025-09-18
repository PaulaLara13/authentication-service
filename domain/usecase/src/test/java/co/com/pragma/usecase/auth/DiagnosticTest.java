package co.com.pragma.usecase.auth;

import co.com.pragma.model.user.Role;
import co.com.pragma.model.user.RoleName;
import co.com.pragma.model.user.User;
import co.com.pragma.model.user.dto.LoginRequest;
import co.com.pragma.model.user.gateways.UserRepository;
import co.com.pragma.model.user.gateways.PasswordHasher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
public class DiagnosticTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordHasher passwordHasher;

    @InjectMocks
    private AuthUseCase authUseCase;

    private User testUser;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        Set<Role> roles = new HashSet<>();
        roles.add(Role.builder().name(RoleName.ROLE_CLIENT).build());
        
        testUser = User.builder()
                .id(BigInteger.ONE)
                .email("test@example.com")
                .password("password123")
                .roles(roles)
                .build();
                
        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");
    }

    @Test
    void givenValidCredentials_whenAuthenticateUser_thenEmitResponse() {
        when(userRepository.findByEmail(anyString())).thenReturn(Mono.just(testUser));
        when(passwordHasher.matches("password123", "password123")).thenReturn(true);

        var mono = authUseCase.authenticateUser(loginRequest);

        StepVerifier.create(mono)
                .expectNextMatches(resp -> resp.getAccessToken() != null && !resp.getAccessToken().isEmpty())
                .verifyComplete();

        verify(userRepository, times(1)).findByEmail(loginRequest.getEmail());
    }
}
