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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DiagnosticTest {
    private static final Logger logger = LoggerFactory.getLogger(DiagnosticTest.class);

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthUseCase authUseCase;

    private User testUser;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        logger.info("Setting up test data...");
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
        
        logger.info("Test user created: {}", testUser);
        logger.info("Login request created: {}", loginRequest);
    }

    @Test
    void testBasicAuthFlow() {
        logger.info("===== STARTING DIAGNOSTIC TEST =====");
        
        try {
            // 1. Verify test setup
            logger.info("1. Verifying test setup...");
            assertNotNull(testUser, "Test user should not be null");
            assertNotNull(loginRequest, "Login request should not be null");
            logger.info("✓ Test setup verified");
            
            // 2. Configure mock
            logger.info("2. Configuring mock for userRepository.findByEmail...");
            when(userRepository.findByEmail(anyString()))
                .thenAnswer(invocation -> {
                    String email = invocation.getArgument(0);
                    logger.info("Mock called with email: {}", email);
                    return Optional.of(testUser);
                });
            logger.info("✓ Mock configured");
            
            // 3. Execute authentication
            logger.info("3. Executing authentication...");
            JwtAuthenticationResponse response = authUseCase.authenticateUser(loginRequest);
            logger.info("✓ Authentication executed");
            
            // 4. Verify response
            logger.info("4. Verifying response...");
            assertNotNull(response, "Response should not be null");
            assertNotNull(response.getAccessToken(), "Access token should not be null");
            assertFalse(response.getAccessToken().isEmpty(), "Access token should not be empty");
            logger.info("✓ Response verified");
            
            // 5. Verify mock interactions
            logger.info("5. Verifying mock interactions...");
            verify(userRepository, times(1)).findByEmail(loginRequest.getEmail());
            logger.info("✓ Mock interactions verified");
            
            logger.info("===== DIAGNOSTIC TEST COMPLETED SUCCESSFULLY =====");
        } catch (Exception e) {
            logger.error("!!!!! DIAGNOSTIC TEST FAILED !!!!!");
            logger.error("Error type: {}", e.getClass().getName());
            logger.error("Error message: {}", e.getMessage());
            logger.error("Stack trace:", e);
            throw e;
        }
    }
}
