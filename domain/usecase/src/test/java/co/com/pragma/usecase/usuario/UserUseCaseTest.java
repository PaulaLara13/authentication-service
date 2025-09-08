package co.com.pragma.usecase.usuario;

import co.com.pragma.model.user.Usuario;
import co.com.pragma.model.user.gateways.UsuarioRepository;
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
    private UsuarioRepository usuarioRepository;

    private UserUseCase userUseCase;

    @BeforeEach
    void setUp() {
        userUseCase = new UserUseCase(usuarioRepository);
    }

    // Utilidad para crear un mock de Usuario con datos válidos
    private Usuario usuarioValidoMock(String name, String lastname, String mail, double salary) {
        // Si getSalary() en tu dominio es double o long, cambia el tipo del último parámetro y del stub correspondiente
        Usuario u = mock(Usuario.class);
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
        Usuario entrada = usuarioValidoMock("John", "Doe", "john.doe@mail.com", salarioEnRango);
        Usuario guardado = mock(Usuario.class);

        when(usuarioRepository.existePorCorreo("john.doe@mail.com")).thenReturn(Mono.just(false));
        when(usuarioRepository.guardar(entrada)).thenReturn(Mono.just(guardado));

        // Act + Assert
        StepVerifier.create(userUseCase.saveUser(entrada))
                .expectNext(guardado)
                .verifyComplete();

        // Verify orden de llamadas
        InOrder inOrder = inOrder(usuarioRepository);
        inOrder.verify(usuarioRepository).existePorCorreo("john.doe@mail.com");
        inOrder.verify(usuarioRepository).guardar(entrada);
        verifyNoMoreInteractions(usuarioRepository);
    }

    @Test
    void saveUser_correoExistente_deberiaEmitirErrorYNoGuardar() {
        // Arrange
        Usuario entrada = usuarioValidoMock("Jane", "Doe", "jane.doe@mail.com", 2500);
        when(usuarioRepository.existePorCorreo("jane.doe@mail.com")).thenReturn(Mono.just(true));

        // Act + Assert
        StepVerifier.create(userUseCase.saveUser(entrada))
                .expectErrorSatisfies(error -> {
                    assertThat(error).isInstanceOf(IllegalArgumentException.class);
                    assertThat(error).hasMessage(MAIL_EXIST);
                })
                .verify();

        // Verify que no intenta guardar
        verify(usuarioRepository).existePorCorreo("jane.doe@mail.com");
        verify(usuarioRepository, never()).guardar(any());
        verifyNoMoreInteractions(usuarioRepository);
    }

    @Test
    void saveUser_nombreInvalido_deberiaLanzarExcepcionSinConsultarRepositorio() {
        // Arrange
        Usuario entrada = mock(Usuario.class);
        when(entrada.getName()).thenReturn(null); // o "" para blanco

        // Act + Assert (excepción sincrónica antes de devolver Mono)
        assertThatThrownBy(() -> userUseCase.saveUser(entrada))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(NAME_VALIDATION);

        verifyNoInteractions(usuarioRepository);
    }

    @Test
    void saveUser_apellidoInvalido_deberiaLanzarExcepcion() {
        // Arrange
        Usuario entrada = mock(Usuario.class);
        when(entrada.getName()).thenReturn("John");
        when(entrada.getLastname()).thenReturn(""); // blanco

        // Act + Assert
        assertThatThrownBy(() -> userUseCase.saveUser(entrada))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(LASTNAME_VALIDATION);

        verifyNoInteractions(usuarioRepository);
    }

    @Test
    void saveUser_correoRequerido_deberiaLanzarExcepcion() {
        // Arrange
        Usuario entrada = mock(Usuario.class);
        when(entrada.getName()).thenReturn("John");
        when(entrada.getLastname()).thenReturn("Doe");
        when(entrada.getMail()).thenReturn("   "); // en blanco

        // Act + Assert
        assertThatThrownBy(() -> userUseCase.saveUser(entrada))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(MAIL_VALIDATION);

        verifyNoInteractions(usuarioRepository);
    }

    @Test
    void saveUser_correoFormatoInvalido_deberiaLanzarExcepcion() {
        // Arrange
        Usuario entrada = mock(Usuario.class);
        when(entrada.getName()).thenReturn("John");
        when(entrada.getLastname()).thenReturn("Doe");
        when(entrada.getMail()).thenReturn("correo-sin-formato");

        // Act + Assert
        assertThatThrownBy(() -> userUseCase.saveUser(entrada))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(MAIL_FORMAT_INVALID);

        verifyNoInteractions(usuarioRepository);
    }

    @Test
    void saveUser_salarioFueraDeRangoInferior_deberiaLanzarExcepcion() {
        // Arrange
        Usuario entrada = mock(Usuario.class);
        when(entrada.getName()).thenReturn("John");
        when(entrada.getLastname()).thenReturn("Doe");
        when(entrada.getMail()).thenReturn("john.doe@mail.com");
        // Un valor claramente bajo; si tu tipo es double, usa -1.0
        when(entrada.getSalary()).thenReturn(-1.0);

        // Act + Assert
        assertThatThrownBy(() -> userUseCase.saveUser(entrada))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(SALARY_RANGE);

        verifyNoInteractions(usuarioRepository);
    }

    @Test
    void saveUser_salarioFueraDeRangoSuperior_deberiaLanzarExcepcion() {
        // Arrange
        Usuario entrada = mock(Usuario.class);
        when(entrada.getName()).thenReturn("John");
        when(entrada.getLastname()).thenReturn("Doe");
        when(entrada.getMail()).thenReturn("john.doe@mail.com");
        // Un valor muy alto; si tu tipo es double, usa 1.0e12
        when(entrada.getSalary()).thenReturn(Double.MAX_VALUE);

        // Act + Assert
        assertThatThrownBy(() -> userUseCase.saveUser(entrada))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(SALARY_RANGE);

        verifyNoInteractions(usuarioRepository);
    }

    @Test
    void getAllUsers_deberiaDelegarEnRepositorio() {
        // Arrange
        Usuario u1 = mock(Usuario.class);
        Usuario u2 = mock(Usuario.class);
        when(usuarioRepository.getAllUsers()).thenReturn(Flux.just(u1, u2));

        // Act + Assert
        StepVerifier.create(userUseCase.getAllUsers())
                .expectNext(u1, u2)
                .verifyComplete();

        verify(usuarioRepository).getAllUsers();
        verifyNoMoreInteractions(usuarioRepository);
    }

    @Test
    void deleteUsuarioId_deberiaInvocarRepositorioYLanzarExcepcion() {
        // Arrange
        BigInteger id = BigInteger.valueOf(123L);
        // Si deleteUsuario retorna Mono<Void> en tu repositorio, puedes stubearlo:
        // when(usuarioRepository.deleteUsuario(id)).thenReturn(Mono.empty());

        // Act + Assert
        assertThatThrownBy(() -> userUseCase.deleteUsuarioId(id))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(DELETE_USER);

        // Verify que se llamó al repositorio con el id correcto antes de la excepción
        verify(usuarioRepository).deleteUsuario(id);
        verifyNoMoreInteractions(usuarioRepository);
    }
}
