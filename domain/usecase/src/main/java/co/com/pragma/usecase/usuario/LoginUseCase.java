package co.com.pragma.usecase.usuario;

import co.com.pragma.api.dto.TokenResponse;
import co.com.pragma.auth.JwtTokenService;
import co.com.pragma.jpa.exception.InvalidCredentialsException;
import co.com.pragma.model.user.gateways.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class LoginUseCase {

    private final UserRepository userRepository; // gateway hexagonal (consulta usuario+rol)
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;

    public Mono<TokenResponse> login(String email, String rawPassword) {
        return userRepository.findByMail(email)
                .switchIfEmpty(Mono.error(new InvalidCredentialsException()))
                .flatMap(u -> passwordEncoder.matches(rawPassword, u.getPasswordHash())
                        ? jwtTokenService.generate(u)
                        : Mono.error(new InvalidCredentialsException()));
    }
}
