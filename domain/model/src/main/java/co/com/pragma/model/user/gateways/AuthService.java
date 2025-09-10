package co.com.pragma.model.user.gateways;

import co.com.pragma.model.user.dto.JwtAuthenticationResponse;
import co.com.pragma.model.user.dto.LoginRequest;

public interface AuthService {
    JwtAuthenticationResponse authenticateUser(LoginRequest loginRequest);
    
    boolean validateToken(String token);
    
    String getUsernameFromToken(String token);
}
