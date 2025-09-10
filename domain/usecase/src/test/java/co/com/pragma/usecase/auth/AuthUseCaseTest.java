package co.com.pragma.usecase.auth;

import co.com.pragma.model.user.Role;
import co.com.pragma.model.user.RoleName;
import co.com.pragma.model.user.User;
import co.com.pragma.model.user.dto.JwtAuthenticationResponse;
import co.com.pragma.model.user.dto.LoginRequest;
import co.com.pragma.model.user.gateways.UserRepository;
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
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class AuthUseCaseTest implements TestExecutionExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(AuthUseCaseTest.class);

    @Mock
    private UserRepository userRepository;

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
    void authenticateUser_Success() {
        try {
            // Given
            logger.info("===== STARTING TEST: authenticateUser_Success =====");
            logger.info("1. Setting up test data...");
            logger.info("Test user: {}", testUser);
            logger.info("Login request: {}", loginRequest);
            
            // Configure mock
            logger.info("2. Configuring mock for userRepository.findByEmail...");
            when(userRepository.findByEmail(anyString()))
                .thenAnswer(invocation -> {
                    String email = invocation.getArgument(0);
                    logger.info("Mock called with email: {}", email);
                    logger.info("Returning test user: {}", testUser);
                    return Optional.of(testUser);
                });
            
            // When
            logger.info("3. Calling authUseCase.authenticateUser...");
            JwtAuthenticationResponse response = authUseCase.authenticateUser(loginRequest);
            logger.info("4. Authentication response received: {}", response);
            
            // Then
            logger.info("5. Verifying response...");
            assertNotNull(response, "Response should not be null");
            logger.info("5.1. Response is not null - PASSED");
            
            logger.info("5.2. Verifying access token...");
            assertNotNull(response.getAccessToken(), "Access token should not be null");
            assertFalse(response.getAccessToken().isEmpty(), "Access token should not be empty");
            logger.info("5.2. Access token is valid - PASSED");
            
            logger.info("5.3. Verifying refresh token...");
            assertNotNull(response.getRefreshToken(), "Refresh token should not be null");
            assertFalse(response.getRefreshToken().isEmpty(), "Refresh token should not be empty");
            logger.info("5.3. Refresh token is valid - PASSED");
            
            // Verify the mock was called with the correct email
            logger.info("6. Verifying mock interactions...");
            verify(userRepository, times(1)).findByEmail(loginRequest.getEmail());
            logger.info("6.1. Mock was called once with correct email - PASSED");
            
            logger.info("===== TEST COMPLETED SUCCESSFULLY =====");
        } catch (Exception e) {
            logger.error("!!!!! TEST FAILED !!!!!");
            logger.error("Error type: {}", e.getClass().getName());
            logger.error("Error message: {}", e.getMessage());
            logger.error("Stack trace:", e);
            
            // Log additional debug information
            logger.error("Current test user: {}", testUser);
            logger.error("Current login request: {}", loginRequest);
            
            // Re-throw the exception to fail the test
            throw e;
        }
    }

    @Test
    void authenticateUser_UserNotFound() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        
        assertThrows(RuntimeException.class, () -> 
            authUseCase.authenticateUser(loginRequest)
        );
    }
    
    @Test
    void authenticateUser_EmptyPassword() {
        testUser.setPassword("");
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        
        assertThrows(RuntimeException.class, () -> 
            authUseCase.authenticateUser(loginRequest)
        );
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
