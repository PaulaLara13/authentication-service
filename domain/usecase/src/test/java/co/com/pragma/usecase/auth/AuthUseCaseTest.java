package co.com.pragma.usecase.auth;

import co.com.pragma.model.user.Role;
import co.com.pragma.model.user.RoleName;
import co.com.pragma.model.user.User;
import co.com.pragma.model.user.dto.JwtAuthenticationResponse;
import co.com.pragma.model.user.dto.LoginRequest;
import co.com.pragma.model.user.gateways.UserRepository;
import co.com.pragma.model.user.gateways.PasswordHasher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class AuthUseCaseTest implements TestExecutionExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(AuthUseCaseTest.class);
    private static final String EMAIL = "test@example.com";
    private static final String PASSWORD = "password123";

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
                .email(EMAIL)
                .password(PASSWORD)
                .roles(roles)
                .build();
                
        loginRequest = new LoginRequest();
        loginRequest.setEmail(EMAIL);
        loginRequest.setPassword(PASSWORD);
    }

    @Test
    void givenValidCredentials_whenAuthenticateUser_thenReturnTokens() {
        when(userRepository.findByEmail(anyString())).thenReturn(Mono.just(testUser));
        when(passwordHasher.matches(PASSWORD, PASSWORD)).thenReturn(true);

        var mono = authUseCase.authenticateUser(loginRequest);

        StepVerifier.create(mono)
                .expectNextMatches(resp -> resp instanceof JwtAuthenticationResponse
                        && resp.getAccessToken() != null
                        && !resp.getAccessToken().isEmpty()
                        && resp.getRefreshToken() != null
                        && !resp.getRefreshToken().isEmpty())
                .verifyComplete();

        verify(userRepository, times(1)).findByEmail(loginRequest.getEmail());
    }

    @Test
    void givenUserNotFound_whenAuthenticateUser_thenThrowInvalidCredentials() {
        when(userRepository.findByEmail(anyString())).thenReturn(Mono.empty());

        var mono = authUseCase.authenticateUser(loginRequest);

        StepVerifier.create(mono)
                .expectErrorMatches(t -> t instanceof co.com.pragma.model.user.exception.InvalidCredentialsException)
                .verify();
    }
    
    @Test
    void givenEmptyPassword_whenAuthenticateUser_thenThrowInvalidCredentials() {
        testUser.setPassword("");
        when(userRepository.findByEmail(anyString())).thenReturn(Mono.just(testUser));
        var mono = authUseCase.authenticateUser(loginRequest);

        StepVerifier.create(mono)
                .expectErrorMatches(t -> t instanceof co.com.pragma.model.user.exception.InvalidCredentialsException)
                .verify();
    }
    
    @Override
    public void handleTestExecutionException(ExtensionContext context, Throwable throwable) throws Throwable {
        Logger logger = LoggerFactory.getLogger(AuthUseCaseTest.class);
        logger.error("Test '{}' failed with exception: {}", 
            context.getDisplayName(), 
            throwable.getMessage(), 
            throwable);
        throw throwable;
    }
}
