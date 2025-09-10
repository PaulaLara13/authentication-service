package co.com.pragma.usecase.auth;

import co.com.pragma.model.user.User;
import co.com.pragma.model.user.dto.JwtAuthenticationResponse;
import co.com.pragma.model.user.dto.LoginRequest;
import co.com.pragma.model.user.gateways.AuthService;
import co.com.pragma.model.user.gateways.UserRepository;
import lombok.RequiredArgsConstructor;
import co.com.pragma.model.user.exception.InvalidCredentialsException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of the authentication use cases.
 * Handles user authentication, token validation, and user retrieval.
 */
@RequiredArgsConstructor
public class AuthUseCase implements AuthService {
    private static final Logger logger = Logger.getLogger(AuthUseCase.class.getName());
    
    private final UserRepository userRepository;

    @Override
    public JwtAuthenticationResponse authenticateUser(LoginRequest loginRequest) {
        logger.log(Level.INFO, "Starting authentication for email: {0}", loginRequest.getEmail());
        
        try {
            logger.log(Level.INFO, "Looking up user by email: {0}", loginRequest.getEmail());
            Optional<User> userOpt = userRepository.findByEmail(loginRequest.getEmail());
            
            if (userOpt.isEmpty()) {
                String errorMsg = String.format("Authentication failed: User not found with email %s", loginRequest.getEmail());
                logger.log(Level.SEVERE, errorMsg);
                throw new InvalidCredentialsException("Invalid credentials");
            }
            
            User user = userOpt.get();
            logger.log(Level.INFO, "User found: {0}", user);
            
            // En una implementación real, aquí se validaría el hash
            // Por ahora validamos igualdad simple (texto plano)
            if (user.getPassword() == null) {
                String errorMsg = String.format("Authentication failed: Null password for user %s", loginRequest.getEmail());
                logger.log(Level.SEVERE, errorMsg);
                throw new InvalidCredentialsException("Invalid credentials");
            }
            
            if (user.getPassword().trim().isEmpty()) {
                String errorMsg = String.format("Authentication failed: Empty password for user %s", loginRequest.getEmail());
                logger.log(Level.SEVERE, errorMsg);
                throw new InvalidCredentialsException("Invalid credentials");
            }
            if (!user.getPassword().equals(loginRequest.getPassword())) {
                String errorMsg = String.format("Authentication failed: Wrong password for user %s", loginRequest.getEmail());
                logger.log(Level.SEVERE, errorMsg);
                throw new InvalidCredentialsException("Invalid credentials");
            }
            
            // In a real implementation, you would generate tokens here
            // For now, we'll return a mock response
            logger.log(Level.INFO, "User authenticated successfully: {0}", loginRequest.getEmail());
            JwtAuthenticationResponse response = JwtAuthenticationResponse.builder()
                    .accessToken("mock-access-token")
                    .refreshToken("mock-refresh-token")
                    .build();
            
            logger.log(Level.INFO, "Generated authentication response: {0}", response);
            return response;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error during authentication: " + e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public boolean validateToken(String token) {
        // In a real implementation, validate the token
        return true;
    }

    @Override
    public String getUsernameFromToken(String token) {
        // In a real implementation, extract username from token
        return "mock-username";
    }

    public Optional<User> getCurrentUser(String email) {
        try {
            Optional<User> user = userRepository.findByEmail(email);
            user.ifPresent(u -> logger.log(Level.INFO, "Current user retrieved: {0}", email));
            return user;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error retrieving current user: {0}", e.getMessage());
            return Optional.empty();
        }
    }
}
