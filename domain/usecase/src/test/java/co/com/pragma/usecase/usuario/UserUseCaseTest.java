package co.com.pragma.usecase.usuario;

import co.com.pragma.model.user.ApiResponse;
import co.com.pragma.model.user.User;
import co.com.pragma.model.user.gateways.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import java.math.BigInteger;
import static co.com.pragma.common.Constants.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)

public class UserUseCaseTest {

    @Mock
    private UserRepository userRepository;

    private UserUseCase userUseCase;

    @BeforeEach
    void setUp() {
        userUseCase = new UserUseCase(userRepository);
    }

    private User usuarioValidoMock(String name, String lastname, String mail, double salary) {
        User u = mock(User.class);
        when(u.getName()).thenReturn(name);
        when(u.getLastname()).thenReturn(lastname);
        when(u.getMail()).thenReturn(mail);
        when(u.getSalary()).thenReturn(salary);
        return u;
    }

    @Test
    void saveUser_exitoso_deberiaValidarExistenciaYGuardar() {
        int salarioEnRango = 2000;
        User entrada = usuarioValidoMock("John", "Doe", "john.doe@mail.com", salarioEnRango);
        User guardado = mock(User.class);
        when(guardado.getId()).thenReturn(BigInteger.valueOf(1));
        when(userRepository.existsByMail("john.doe@mail.com")).thenReturn(Mono.just(false));
        when(userRepository.saveUser(entrada)).thenReturn(Mono.just(guardado));

        StepVerifier.create(userUseCase.saveUser(entrada))
                .assertNext(resp -> assertThat(resp.getMensaje()).contains(REQUESTS_CREATEID))
                .verifyComplete();

        InOrder inOrder = inOrder(userRepository);
        inOrder.verify(userRepository).existsByMail("john.doe@mail.com");
        inOrder.verify(userRepository).saveUser(entrada);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void saveUser_correoExistente_deberiaEmitirErrorYNoGuardar() {
        User entrada = usuarioValidoMock("Jane", "Doe", "jane.doe@mail.com", 2500);
        when(userRepository.existsByMail("jane.doe@mail.com")).thenReturn(Mono.just(true));

        StepVerifier.create(userUseCase.saveUser(entrada))
                .expectErrorSatisfies(error -> {
                    assertThat(error).isInstanceOf(IllegalArgumentException.class);
                    assertThat(error).hasMessage(MAIL_EXIST);
                })
                .verify();

        verify(userRepository).existsByMail("jane.doe@mail.com");
        verify(userRepository, never()).saveUser(any());
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void saveUser_nombreInvalido_deberiaLanzarExcepcionSinConsultarRepositorio() {
        User entrada = mock(User.class);
        when(entrada.getName()).thenReturn(null);

        assertThatThrownBy(() -> userUseCase.saveUser(entrada))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(NAME_VALIDATION);

        verifyNoInteractions(userRepository);
    }

    @Test
    void saveUser_apellidoInvalido_deberiaLanzarExcepcion() {
        User entrada = mock(User.class);
        when(entrada.getName()).thenReturn("John");
        when(entrada.getLastname()).thenReturn("");

        assertThatThrownBy(() -> userUseCase.saveUser(entrada))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(LASTNAME_VALIDATION);

        verifyNoInteractions(userRepository);
    }

    @Test
    void saveUser_correoRequerido_deberiaLanzarExcepcion() {
        User entrada = mock(User.class);
        when(entrada.getName()).thenReturn("John");
        when(entrada.getLastname()).thenReturn("Doe");
        when(entrada.getMail()).thenReturn("   ");

        assertThatThrownBy(() -> userUseCase.saveUser(entrada))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(MAIL_VALIDATION);

        verifyNoInteractions(userRepository);
    }

    @Test
    void saveUser_correoFormatoInvalido_deberiaLanzarExcepcion() {
        User entrada = mock(User.class);
        when(entrada.getName()).thenReturn("John");
        when(entrada.getLastname()).thenReturn("Doe");
        when(entrada.getMail()).thenReturn("correo-sin-formato");

        assertThatThrownBy(() -> userUseCase.saveUser(entrada))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(MAIL_FORMAT_INVALID);

        verifyNoInteractions(userRepository);
    }

    @Test
    void saveUser_salarioFueraDeRangoInferior_deberiaLanzarExcepcion() {
        User entrada = mock(User.class);
        when(entrada.getName()).thenReturn("John");
        when(entrada.getLastname()).thenReturn("Doe");
        when(entrada.getMail()).thenReturn("john.doe@mail.com");
        when(entrada.getSalary()).thenReturn(-1.0);

        assertThatThrownBy(() -> userUseCase.saveUser(entrada))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(SALARY_RANGE);

        verifyNoInteractions(userRepository);
    }

    @Test
    void saveUser_salarioFueraDeRangoSuperior_deberiaLanzarExcepcion() {
        User entrada = mock(User.class);
        when(entrada.getName()).thenReturn("John");
        when(entrada.getLastname()).thenReturn("Doe");
        when(entrada.getMail()).thenReturn("john.doe@mail.com");
        when(entrada.getSalary()).thenReturn(Double.MAX_VALUE);

        assertThatThrownBy(() -> userUseCase.saveUser(entrada))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(SALARY_RANGE);

        verifyNoInteractions(userRepository);
    }

    @Test
    void getAllUsers_deberiaDelegarEnRepositorio() {
        User u1 = mock(User.class);
        User u2 = mock(User.class);
        when(userRepository.getAllUsers()).thenReturn(Flux.just(u1, u2));

        StepVerifier.create(userUseCase.getAllUsers())
                .expectNext(u1, u2)
                .verifyComplete();

        verify(userRepository).getAllUsers();
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void deleteUsuarioId_deberiaInvocarRepositorioYLanzarExcepcion() {
        BigInteger id = BigInteger.valueOf(123L);

        assertThatThrownBy(() -> userUseCase.deleteUserId(id))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(DELETE_USER);

        verify(userRepository).deleteUser(id);
        verifyNoMoreInteractions(userRepository);
    }
}
