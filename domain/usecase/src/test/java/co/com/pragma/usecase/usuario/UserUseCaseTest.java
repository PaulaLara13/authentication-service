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

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

import static co.com.pragma.common.Constants.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserUseCaseTest {

    @Mock
    private UserRepository userRepository;

    private UserUseCase userUseCase;

    @BeforeEach
    void setUp() {
        userUseCase = new UserUseCase(userRepository);
    }

    private User createValidUser(String name, String lastname, String email, double salary) {
        return User.builder()
                .name(name)
                .lastname(lastname)
                .email(email)
                .salary(salary)
                .build();
    }

    @Test
    void saveUser_shouldValidateAndSaveUser() {
        // Arrange
        User user = createValidUser("John", "Doe", "john.doe@mail.com", 2000);
        User savedUser = User.builder()
                .id(BigInteger.ONE)
                .email("john.doe@mail.com")
                .build();

        when(userRepository.existsByMail("john.doe@mail.com")).thenReturn(false);
        when(userRepository.saveUser(user)).thenReturn(savedUser);

        // Act
        ApiResponse response = userUseCase.saveUser(user);

        // Assert
        assertThat(response.getMensaje()).contains(REQUESTS_CREATEID);

        InOrder inOrder = inOrder(userRepository);
        inOrder.verify(userRepository).existsByMail("john.doe@mail.com");
        inOrder.verify(userRepository).saveUser(user);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void saveUser_shouldThrowExceptionWhenEmailExists() {
        // Arrange
        User user = createValidUser("Jane", "Doe", "jane.doe@mail.com", 2500);
        when(userRepository.existsByMail("jane.doe@mail.com")).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> userUseCase.saveUser(user))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(MAIL_EXIST);

        verify(userRepository).existsByMail("jane.doe@mail.com");
        verify(userRepository, never()).saveUser(any());
    }

    @Test
    void saveUser_shouldValidateName() {
        // Arrange
        User user = createValidUser("", "Doe", "john.doe@mail.com", 2000);

        // Act & Assert
        assertThatThrownBy(() -> userUseCase.saveUser(user))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(NAME_VALIDATION);

        verifyNoInteractions(userRepository);
    }

    @Test
    void saveUser_shouldValidateLastname() {
        // Arrange
        User user = createValidUser("John", "", "john.doe@mail.com", 2000);

        // Act & Assert
        assertThatThrownBy(() -> userUseCase.saveUser(user))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(LASTNAME_VALIDATION);

        verifyNoInteractions(userRepository);
    }

    @Test
    void saveUser_shouldValidateEmail() {
        // Arrange
        User user = createValidUser("John", "Doe", "", 2000);

        // Act & Assert
        assertThatThrownBy(() -> userUseCase.saveUser(user))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(MAIL_VALIDATION);

        verifyNoInteractions(userRepository);
    }

    @Test
    void saveUser_shouldValidateEmailFormat() {
        // Arrange
        User user = createValidUser("John", "Doe", "invalid-email", 2000);

        // Act & Assert
        assertThatThrownBy(() -> userUseCase.saveUser(user))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(MAIL_FORMAT_INVALID);

        verifyNoInteractions(userRepository);
    }

    @Test
    void saveUser_shouldValidateSalaryRange() {
        // Test lower bound
        User user1 = createValidUser("John", "Doe", "john@example.com", -1);
        assertThatThrownBy(() -> userUseCase.saveUser(user1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(SALARY_RANGE);

        // Test upper bound
        User user2 = createValidUser("Jane", "Doe", "jane@example.com", Double.MAX_VALUE);
        assertThatThrownBy(() -> userUseCase.saveUser(user2))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(SALARY_RANGE);

        verifyNoInteractions(userRepository);
    }

    @Test
    void getAllUsers_shouldReturnAllUsers() {
        // Arrange
        User user1 = createValidUser("John", "Doe", "john@example.com", 2000);
        User user2 = createValidUser("Jane", "Doe", "jane@example.com", 2500);
        List<User> users = Arrays.asList(user1, user2);

        when(userRepository.getAllUsers()).thenReturn(users);

        // Act
        List<User> result = userUseCase.getAllUsers();

        // Assert
        assertThat(result).hasSize(2);
        assertThat(result).containsExactlyInAnyOrder(user1, user2);
        verify(userRepository).getAllUsers();
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void deleteUser_shouldCallRepository() {
        // Arrange
        BigInteger userId = BigInteger.ONE;

        // Act & Assert
        assertThatThrownBy(() -> userUseCase.deleteUser(userId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(DELETE_USER);

        verify(userRepository).deleteUser(userId);
        verifyNoMoreInteractions(userRepository);
    }
}
