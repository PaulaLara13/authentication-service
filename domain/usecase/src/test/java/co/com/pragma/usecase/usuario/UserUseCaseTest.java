package co.com.pragma.usecase.usuario;

import co.com.pragma.model.user.ApiResponse;
import co.com.pragma.model.user.User;
import co.com.pragma.model.user.gateways.RoleRepository;
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

    @Mock
    private RoleRepository roleRepository;

    private UserUseCase userUseCase;

    @BeforeEach
    void setUp() {
        userUseCase = new UserUseCase(userRepository, roleRepository);
    }

    // Utilidad para crear un mock de Usuario con datos válidos
    private User usuarioValidoMock(String name, String lastname, String mail, double salary) {
        // Si getSalary() en tu dominio es double o long, cambia el tipo del último parámetro y del stub correspondiente
        User u = mock(User.class);
        when(u.getName()).thenReturn(name);
        when(u.getLastname()).thenReturn(lastname);
        when(u.getMail()).thenReturn(mail);
        when(u.getSalary()).thenReturn(salary);
        return u;
    }

    @Test
    void saveUser_exitoso_deberiaValidarExistenciaYGuardar() {
        // Arrange
        // Ajusta el valor del salario si tus constantes de rango requieren double: p.ej. 2000.0
        int salarioEnRango = 2000;
        User entrada = usuarioValidoMock("John", "Doe", "john.doe@mail.com", salarioEnRango);
        User guardado = mock(User.class);
        ApiResponse response = new ApiResponse("Solicitud creada con éxito. ID:");

        when(userRepository.existsByMail("john.doe@mail.com")).thenReturn(Mono.just(false));
        when(userRepository.saveUser(entrada)).thenReturn(Mono.just(guardado));

        // Act + Assert
        StepVerifier.create(userUseCase.saveUser(entrada))
                .expectNext(response)
                .verifyComplete();

        // Verify orden de llamadas
        InOrder inOrder = inOrder(userRepository);
        inOrder.verify(userRepository).existsByMail("john.doe@mail.com");
        inOrder.verify(userRepository).saveUser(entrada);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void saveUser_correoExistente_deberiaEmitirErrorYNoGuardar() {
        // Arrange
        User entrada = usuarioValidoMock("Jane", "Doe", "jane.doe@mail.com", 2500);
        when(userRepository.existsByMail("jane.doe@mail.com")).thenReturn(Mono.just(true));

        // Act + Assert
        StepVerifier.create(userUseCase.saveUser(entrada))
                .expectErrorSatisfies(error -> {
                    assertThat(error).isInstanceOf(IllegalArgumentException.class);
                    assertThat(error).hasMessage(MAIL_EXIST);
                })
                .verify();

        // Verify que no intenta guardar
        verify(userRepository).existsByMail("jane.doe@mail.com");
        verify(userRepository, never()).saveUser(any());
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void saveUser_nombreInvalido_deberiaLanzarExcepcionSinConsultarRepositorio() {
        // Arrange
        User entrada = mock(User.class);
        when(entrada.getName()).thenReturn(null); // o "" para blanco

        // Act + Assert (excepción sincrónica antes de devolver Mono)
        assertThatThrownBy(() -> userUseCase.saveUser(entrada))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(NAME_VALIDATION);

        verifyNoInteractions(userRepository);
    }

    @Test
    void saveUser_apellidoInvalido_deberiaLanzarExcepcion() {
        // Arrange
        User entrada = mock(User.class);
        when(entrada.getName()).thenReturn("John");
        when(entrada.getLastname()).thenReturn(""); // blanco

        // Act + Assert
        assertThatThrownBy(() -> userUseCase.saveUser(entrada))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(LASTNAME_VALIDATION);

        verifyNoInteractions(userRepository);
    }

    @Test
    void saveUser_correoRequerido_deberiaLanzarExcepcion() {
        // Arrange
        User entrada = mock(User.class);
        when(entrada.getName()).thenReturn("John");
        when(entrada.getLastname()).thenReturn("Doe");
        when(entrada.getMail()).thenReturn("   "); // en blanco

        // Act + Assert
        assertThatThrownBy(() -> userUseCase.saveUser(entrada))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(MAIL_VALIDATION);

        verifyNoInteractions(userRepository);
    }

    @Test
    void saveUser_correoFormatoInvalido_deberiaLanzarExcepcion() {
        // Arrange
        User entrada = mock(User.class);
        when(entrada.getName()).thenReturn("John");
        when(entrada.getLastname()).thenReturn("Doe");
        when(entrada.getMail()).thenReturn("correo-sin-formato");

        // Act + Assert
        assertThatThrownBy(() -> userUseCase.saveUser(entrada))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(MAIL_FORMAT_INVALID);

        verifyNoInteractions(userRepository);
    }

    @Test
    void saveUser_salarioFueraDeRangoInferior_deberiaLanzarExcepcion() {
        // Arrange
        User entrada = mock(User.class);
        when(entrada.getName()).thenReturn("John");
        when(entrada.getLastname()).thenReturn("Doe");
        when(entrada.getMail()).thenReturn("john.doe@mail.com");
        // Un valor claramente bajo; si tu tipo es double, usa -1.0
        when(entrada.getSalary()).thenReturn(-1.0);

        // Act + Assert
        assertThatThrownBy(() -> userUseCase.saveUser(entrada))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(SALARY_RANGE);

        verifyNoInteractions(userRepository);
    }

    @Test
    void saveUser_salarioFueraDeRangoSuperior_deberiaLanzarExcepcion() {
        // Arrange
        User entrada = mock(User.class);
        when(entrada.getName()).thenReturn("John");
        when(entrada.getLastname()).thenReturn("Doe");
        when(entrada.getMail()).thenReturn("john.doe@mail.com");
        // Un valor muy alto; si tu tipo es double, usa 1.0e12
        when(entrada.getSalary()).thenReturn(Double.MAX_VALUE);

        // Act + Assert
        assertThatThrownBy(() -> userUseCase.saveUser(entrada))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(SALARY_RANGE);

        verifyNoInteractions(userRepository);
    }

    @Test
    void getAllUsers_deberiaDelegarEnRepositorio() {
        // Arrange
        User u1 = mock(User.class);
        User u2 = mock(User.class);
        when(userRepository.getAllUsers()).thenReturn(Flux.just(u1, u2));

        // Act + Assert
        StepVerifier.create(userUseCase.getAllUsers())
                .expectNext(u1, u2)
                .verifyComplete();

        verify(userRepository).getAllUsers();
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void deleteUsuarioId_deberiaInvocarRepositorioYLanzarExcepcion() {
        // Arrange
        BigInteger id = BigInteger.valueOf(123L);
        // Si deleteUsuario retorna Mono<Void> en tu repositorio, puedes stubearlo:
        // when(usuarioRepository.deleteUsuario(id)).thenReturn(Mono.empty());

        // Act + Assert
        assertThatThrownBy(() -> userUseCase.deleteUserId(id))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(DELETE_USER);

        // Verify que se llamó al repositorio con el id correcto antes de la excepción
        verify(userRepository).deleteUser(id);
        verifyNoMoreInteractions(userRepository);
    }
}
