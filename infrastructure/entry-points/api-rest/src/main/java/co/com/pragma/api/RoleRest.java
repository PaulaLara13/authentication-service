package co.com.pragma.api;

import co.com.pragma.api.dto.LoginRequest;
import co.com.pragma.api.dto.TokenResponse;
import co.com.pragma.usecase.usuario.LoginUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@RestController
@RequestMapping(value = "/api/v1", produces = MediaType.APPLICATION_JSON_VALUE)
public class RoleRest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RoleRest.class);
    private final LoginUseCase loginUseCase;

    @PostMapping("/login")
    public Mono<ResponseEntity<TokenResponse>> login(@RequestBody Mono<LoginRequest> body) {
        return body
                .doOnNext(req -> log.info("START_LOGIN email={}", req.email()))
                .flatMap(req -> loginUseCase.login(req.email(), req.password()))
                .doOnSuccess(t -> log.info("LOGIN_SUCCESS"))
                .map(ResponseEntity::ok);
    }

}
