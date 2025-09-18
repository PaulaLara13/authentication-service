package co.com.pragma.api;

import co.com.pragma.api.dto.CreateUserDto;
import co.com.pragma.api.dto.UserDto;
import co.com.pragma.api.mapper.UserDtoMapper;
import co.com.pragma.model.user.ApiResponse;
import co.com.pragma.model.user.User;
import co.com.pragma.usecase.usuario.UserUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

import java.math.BigInteger;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import co.com.pragma.model.user.gateways.PasswordHasher;

class ApiRestWebFluxTest {

    private WebTestClient webTestClient;
    private UserUseCase userUseCase;
    private UserDtoMapper userDtoMapper;
    private ApiRest apiRest;
    private PasswordHasher passwordHasher;

    @BeforeEach
    void init() {
        userUseCase = Mockito.mock(UserUseCase.class);
        userDtoMapper = Mockito.mock(UserDtoMapper.class);
        passwordHasher = Mockito.mock(PasswordHasher.class);
        Mockito.when(passwordHasher.encode(any())).thenAnswer(inv -> inv.getArgument(0));
        apiRest = new ApiRest(userUseCase, userDtoMapper, passwordHasher);
        webTestClient = WebTestClient.bindToController(apiRest).build();
    }

    @Test
    void givenValidPayload_whenCreateUser_then201() {
        var dto = new CreateUserDto(null, "New", "User", LocalDate.parse("1990-01-01"),
                "Addr", "3000000", "new@demo.com", 1000.0, "secret", "ADMIN");
        var model = User.builder().email("new@demo.com").name("New").lastname("User").build();
        Mockito.when(userDtoMapper.toModel(eq(dto))).thenReturn(model);
        Mockito.when(userUseCase.saveUser(eq(model))).thenReturn(Mono.just(new ApiResponse("Created")));

        webTestClient.post()
                .uri("/api/v1/user")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(dto), CreateUserDto.class)
                .exchange()
                .expectStatus().isCreated();
    }

    @Test
    void givenRequest_whenGetAllUsers_then200() {
        var u = User.builder().email("a@b.com").name("A").lastname("B").build();
        Mockito.when(userUseCase.getAllUsers()).thenReturn(Flux.just(u));
        Mockito.when(userDtoMapper.toResponse(any(User.class))).thenReturn(
                new UserDto(null, "A", "B", LocalDate.of(1990,1,1), "", "", "a@b.com", 0.0, null)
        );

        webTestClient.get()
                .uri("/api/v1/user")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void givenId_whenDeleteUser_then200() {
        Mockito.when(userUseCase.deleteUser(eq(BigInteger.ONE))).thenReturn(Mono.empty());
        webTestClient.delete()
                .uri("/api/v1/user/1")
                .exchange()
                .expectStatus().isOk();
    }
}
