package co.com.pragma.model.user.gateways;

import co.com.pragma.model.user.dto.JwtAuthenticationResponse;
import co.com.pragma.model.user.dto.LoginRequest;
import reactor.core.publisher.Mono;

public interface AuthService {
    Mono<JwtAuthenticationResponse> authenticateUser(LoginRequest loginRequest);
    
    Mono<Boolean> validateToken(String token);
    
    Mono<String> getUsernameFromToken(String token);
}
