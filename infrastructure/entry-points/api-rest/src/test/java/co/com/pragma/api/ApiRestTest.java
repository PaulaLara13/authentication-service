package co.com.pragma.api;

import co.com.pragma.api.dto.CreateUserDto;
import co.com.pragma.api.dto.UserDto;
import co.com.pragma.api.mapper.UserDtoMapper;
import co.com.pragma.usecase.usuario.UserUseCase;
import co.com.pragma.model.user.Usuario;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import java.math.BigInteger;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)

class ApiRestTest {
    @Mock
    private UserUseCase userUseCase;

    @Mock
    private UserDtoMapper userMapper;

    @InjectMocks
    private ApiRest apiRest;

    @Test
    void crearUsuario_deberiaRetornar201ConCuerpo() {
        // Arrange
        CreateUserDto createUserDto = mock(CreateUserDto.class);
        Usuario usuario = mock(Usuario.class);
        Usuario usuarioGuardado = mock(Usuario.class);
        UserDto userDtoEsperado = mock(UserDto.class);

        when(userMapper.toModel(createUserDto)).thenReturn(usuario);
        when(userUseCase.saveUser(usuario)).thenReturn(Mono.just(usuarioGuardado));
        when(userMapper.toResponse(usuarioGuardado)).thenReturn(userDtoEsperado);

        // Act + Assert
        StepVerifier.create(apiRest.createUser(createUserDto))
                .assertNext(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
                    assertThat(response.getBody()).isSameAs(userDtoEsperado);
                })
                .verifyComplete();

        // Verify
        verify(userMapper).toModel(createUserDto);
        verify(userUseCase).saveUser(usuario);
        verify(userMapper).toResponse(usuarioGuardado);
        verifyNoMoreInteractions(userUseCase, userMapper);
    }

    @Test
    void obtenerTodos_deberiaRetornar200ConLista() {
        // Arrange
        Usuario usuario1 = mock(Usuario.class);
        Usuario usuario2 = mock(Usuario.class);
        UserDto dto1 = mock(UserDto.class);
        UserDto dto2 = mock(UserDto.class);

        when(userUseCase.getAllUsers()).thenReturn(Flux.just(usuario1, usuario2));
        when(userMapper.toResponse(usuario1)).thenReturn(dto1);
        when(userMapper.toResponse(usuario2)).thenReturn(dto2);

        // Act + Assert
        StepVerifier.create(apiRest.getAllUser())
                .assertNext(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                    assertThat(response.getBody()).containsExactly(dto1, dto2);
                })
                .verifyComplete();

        // Verify
        verify(userUseCase).getAllUsers();
        verify(userMapper).toResponse(usuario1);
        verify(userMapper).toResponse(usuario2);
        verifyNoMoreInteractions(userUseCase, userMapper);
    }

    @Test
    void obtenerTodos_deberiaRetornar200ConListaVacia() {
        // Arrange
        when(userUseCase.getAllUsers()).thenReturn(Flux.empty());

        // Act + Assert
        StepVerifier.create(apiRest.getAllUser())
                .assertNext(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                    assertThat(response.getBody()).isEmpty();
                })
                .verifyComplete();

        // Verify
        verify(userUseCase).getAllUsers();
        verifyNoMoreInteractions(userUseCase);
        verifyNoInteractions(userMapper);
    }

    @Test
    void eliminarUsuario_deberiaInvocarCasoDeUsoYRetornar200() {
        // Arrange
        BigInteger id = BigInteger.valueOf(123L);
        when(userUseCase.deleteUsuarioId(id)).thenReturn(Mono.empty());

        // Act + Assert
        StepVerifier.create(apiRest.deleteUsuario(id))
                .assertNext(response -> {
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
                    assertThat(response.getBody()).isNull();
                })
                .verifyComplete();

        // Verify que se llamó con el id correcto
        ArgumentCaptor<BigInteger> idCaptor = ArgumentCaptor.forClass(BigInteger.class);
        verify(userUseCase).deleteUsuarioId(idCaptor.capture());
        assertThat(idCaptor.getValue()).isEqualTo(id);

        verifyNoMoreInteractions(userUseCase);
        verifyNoInteractions(userMapper);
    }
}
