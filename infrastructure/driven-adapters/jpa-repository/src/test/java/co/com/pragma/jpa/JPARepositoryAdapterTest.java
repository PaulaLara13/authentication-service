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

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JPARepositoryAdapterTest {
    
    @Mock
    private JPARepository repository;

    @Mock
    private ObjectMapper mapper;

    private JPARepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = spy(new JPARepositoryAdapter(repository, mapper));
    }

    @Test
    void saveUser_shouldMapSaveAndReturnDomain() {
        // Arrange
        User inputUser = mock(User.class);
        UserEntity entityToSave = mock(UserEntity.class);
        UserEntity savedEntity = mock(UserEntity.class);
        User mappedUser = mock(User.class);

        doReturn(entityToSave).when(adapter).toData(inputUser);
        when(repository.save(entityToSave)).thenReturn(savedEntity);
        doReturn(mappedUser).when(adapter).toEntity(savedEntity);

        // Act
        User result = adapter.saveUser(inputUser);

        // Assert
        assertThat(result).isSameAs(mappedUser);

        InOrder inOrder = inOrder(adapter, repository);
        inOrder.verify(adapter).toData(inputUser);
        inOrder.verify(repository).save(entityToSave);
        inOrder.verify(adapter).toEntity(savedEntity);

        verifyNoMoreInteractions(repository);
    }

    @Test
    void existsByMail_whenEmailExists_shouldReturnTrue() {
        // Arrange
        String email = "test@example.com";
        when(repository.existsByMail(email)).thenReturn(true);

        // Act
        boolean result = adapter.existsByMail(email);

        // Assert
        assertThat(result).isTrue();
        verify(repository).existsByMail(email);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void existsByMail_whenEmailDoesNotExist_shouldReturnFalse() {
        // Arrange
        String email = "nonexistent@example.com";
        when(repository.existsByMail(email)).thenReturn(false);

        // Act
        boolean result = adapter.existsByMail(email);

        // Assert
        assertThat(result).isFalse();
        verify(repository).existsByMail(email);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void getAllUsers_shouldReturnMappedUsers() {
        // Arrange
        UserEntity entity1 = mock(UserEntity.class);
        UserEntity entity2 = mock(UserEntity.class);
        List<UserEntity> entities = List.of(entity1, entity2);

        User user1 = mock(User.class);
        User user2 = mock(User.class);
        List<User> users = List.of(user1, user2);

        when(repository.findAll()).thenReturn(entities);
        doReturn(users).when(adapter).toList(any(Iterable.class));

        // Act
        List<User> result = adapter.getAllUsers();

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result).containsExactlyInAnyOrder(user1, user2);
        
        verify(repository).findAll();
        verify(adapter).toList(entities);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void deleteUser_whenUserExists_shouldDeleteUser() {
        // Arrange
        BigInteger userId = BigInteger.ONE;
        when(repository.existsById(userId)).thenReturn(true);

        // Act
        adapter.deleteUser(userId);

        // Assert
        verify(repository).existsById(userId);
        verify(repository).deleteById(userId);
        verifyNoMoreInteractions(repository);
    }

    @Test
    void deleteUser_whenUserDoesNotExist_shouldThrowException() {
        // Arrange
        BigInteger userId = BigInteger.valueOf(99);
        when(repository.existsById(userId)).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> adapter.deleteUser(userId))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found with id: 99");

        verify(repository).existsById(userId);
        verify(repository, never()).deleteById(any());
        verifyNoMoreInteractions(repository);
    }
    
    @Test
    void findByEmail_whenUserExists_shouldReturnUser() {
        // Arrange
        String email = "user@example.com";
        UserEntity userEntity = mock(UserEntity.class);
        User expectedUser = mock(User.class);
        
        when(repository.findByMail(email)).thenReturn(userEntity);
        when(adapter.toEntity(userEntity)).thenReturn(expectedUser);
        
        // Act
        Optional<User> result = adapter.findByEmail(email);
        
        // Assert
        assertThat(result).isPresent().contains(expectedUser);
        verify(repository).findByMail(email);
        verify(adapter).toEntity(userEntity);
        verifyNoMoreInteractions(repository);
    }
    
    @Test
    void findByEmail_whenUserDoesNotExist_shouldReturnEmpty() {
        // Arrange
        String email = "nonexistent@example.com";
        when(repository.findByMail(email)).thenReturn(null);
        
        // Act
        Optional<User> result = adapter.findByEmail(email);
        
        // Assert
        assertThat(result).isEmpty();
        verify(repository).findByMail(email);
        verifyNoMoreInteractions(repository, mapper);
    }
}
