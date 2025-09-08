package co.com.pragma.jpa;
import co.com.pragma.jpa.entity.UserEntity;
import co.com.pragma.jpa.exception.UserNotFoundException;
import co.com.pragma.model.user.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.reactivecommons.utils.ObjectMapper;
import reactor.test.StepVerifier;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)

public class JPARepositoryAdapterTest {
    @Mock
    private JPARepository repository;

    @Mock
    private ObjectMapper mapper;

    private JPARepositoryAdapter adapter; // spy en @BeforeEach

    @BeforeEach
    void setUp() {
        adapter = spy(new JPARepositoryAdapter(repository, mapper));
    }

    @Test
    void guardar_deberiaMapearGuardarYRetornarDominio() {
        // Arrange
        User usuarioEntrada = mock(User.class);
        UserEntity entidadParaGuardar = mock(UserEntity.class);
        UserEntity entidadGuardada = mock(UserEntity.class);
        User usuarioMapeado = mock(User.class);

        doReturn(entidadParaGuardar).when(adapter).toData(usuarioEntrada);
        when(repository.save(entidadParaGuardar)).thenReturn(entidadGuardada);
        doReturn(usuarioMapeado).when(adapter).toEntity(entidadGuardada);

        // Act + Assert
        StepVerifier.create(adapter.saveUser(usuarioEntrada))
                .expectNext(usuarioMapeado)
                .verifyComplete();

        // Verify orden y llamadas
        InOrder inOrder = inOrder(adapter, repository);
        inOrder.verify(adapter).toData(usuarioEntrada);
        inOrder.verify(repository).save(entidadParaGuardar);
        inOrder.verify(adapter).toEntity(entidadGuardada);

        verifyNoMoreInteractions(repository);
    }

    @Test
    void existePorCorreo_true_deberiaRetornarTrue() {
        // Arrange
        String correo = "mail@dominio.com";
        when(repository.existsByMail(correo)).thenReturn(true);

        // Act + Assert
        StepVerifier.create(adapter.existsByMail(correo))
                .expectNext(true)
                .verifyComplete();

        verify(repository).existsByMail(correo);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void existePorCorreo_false_deberiaRetornarFalse() {
        // Arrange
        String correo = "otro@dominio.com";
        when(repository.existsByMail(correo)).thenReturn(false);

        // Act + Assert
        StepVerifier.create(adapter.existsByMail(correo))
                .expectNext(false)
                .verifyComplete();

        verify(repository).existsByMail(correo);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void getAllUsers_deberiaRetornarUsuariosMapeados() {
        // Arrange
        UserEntity e1 = mock(UserEntity.class);
        UserEntity e2 = mock(UserEntity.class);
        List<UserEntity> entidades = List.of(e1, e2);

        User u1 = mock(User.class);
        User u2 = mock(User.class);
        List<User> usuarios = List.of(u1, u2);

        when(repository.findAll()).thenReturn(entidades);
        // Stub del método protegido toList
        doReturn(usuarios).when(adapter).toList(any(Iterable.class));

        // Act + Assert
        StepVerifier.create(adapter.getAllUsers())
                .expectNext(u1, u2)
                .verifyComplete();

        verify(repository).findAll();
        verify(adapter).toList(entidades);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void deleteUsuario_existente_deberiaEliminarYCompletar() {
        // Arrange
        BigInteger id = BigInteger.valueOf(1);
        UserEntity usuarioEntity = mock(UserEntity.class);
        when(repository.findById(id)).thenReturn(Optional.of(usuarioEntity));
        when(usuarioEntity.getId()).thenReturn(id);

        // Act + Assert
        StepVerifier.create(adapter.deleteUser(id))
                .verifyComplete();

        verify(repository).findById(id);
        verify(repository).deleteById(id);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void deleteUsuario_noExistente_deberiaEmitirUserNotFoundException() {
        // Arrange
        BigInteger id = BigInteger.valueOf(99);
        when(repository.findById(id)).thenReturn(Optional.empty());

        // Act + Assert
        StepVerifier.create(adapter.deleteUser(id))
                .expectErrorSatisfies(throwable ->
                        assertThat(throwable).isInstanceOf(UserNotFoundException.class))
                .verify();

        verify(repository).findById(id);
        verify(repository, never()).deleteById(any());
        verifyNoMoreInteractions(repository);
    }
}
