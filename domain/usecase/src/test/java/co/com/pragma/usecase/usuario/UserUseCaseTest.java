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

import static co.com.pragma.common.Constants.*;
import static org.mockito.Mockito.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

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
    void givenValidUser_whenSaveUser_thenReturnCreatedMessage() {
        var user = createValidUser("John", "Doe", "john.doe@mail.com", 2000);
        var savedUser = User.builder().id(BigInteger.ONE).email("john.doe@mail.com").build();

        when(userRepository.existsByMail("john.doe@mail.com")).thenReturn(Mono.just(false));
        when(userRepository.saveUser(user)).thenReturn(Mono.just(savedUser));

        var mono = userUseCase.saveUser(user);

        StepVerifier.create(mono)
                .expectNextMatches(resp -> resp instanceof ApiResponse && resp.getMensaje().contains(REQUESTS_CREATEID))
                .verifyComplete();

        InOrder inOrder = inOrder(userRepository);
        inOrder.verify(userRepository).existsByMail("john.doe@mail.com");
        inOrder.verify(userRepository).saveUser(user);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void givenExistingEmail_whenSaveUser_thenErrorMailExist() {
        var user = createValidUser("Jane", "Doe", "jane.doe@mail.com", 2500);
        when(userRepository.existsByMail("jane.doe@mail.com")).thenReturn(Mono.just(true));

        var mono = userUseCase.saveUser(user);

        StepVerifier.create(mono)
                .expectErrorMatches(t -> t instanceof IllegalArgumentException && MAIL_EXIST.equals(t.getMessage()))
                .verify();

        verify(userRepository).existsByMail("jane.doe@mail.com");
        verify(userRepository, never()).saveUser(any());
    }

    @Test
    void givenInvalidName_whenSaveUser_thenErrorNameValidation() {
        var user = createValidUser("", "Doe", "john.doe@mail.com", 2000);
        var mono = userUseCase.saveUser(user);
        StepVerifier.create(mono)
                .expectErrorMatches(t -> t instanceof IllegalArgumentException && NAME_VALIDATION.equals(t.getMessage()))
                .verify();
        verifyNoInteractions(userRepository);
    }

    @Test
    void givenInvalidLastname_whenSaveUser_thenErrorLastnameValidation() {
        var user = createValidUser("John", "", "john.doe@mail.com", 2000);
        var mono = userUseCase.saveUser(user);
        StepVerifier.create(mono)
                .expectErrorMatches(t -> t instanceof IllegalArgumentException && LASTNAME_VALIDATION.equals(t.getMessage()))
                .verify();
        verifyNoInteractions(userRepository);
    }

    @Test
    void givenEmptyEmail_whenSaveUser_thenErrorMailValidation() {
        var user = createValidUser("John", "Doe", "", 2000);
        var mono = userUseCase.saveUser(user);
        StepVerifier.create(mono)
                .expectErrorMatches(t -> t instanceof IllegalArgumentException && MAIL_VALIDATION.equals(t.getMessage()))
                .verify();
        verifyNoInteractions(userRepository);
    }

    @Test
    void givenInvalidEmailFormat_whenSaveUser_thenErrorMailFormatInvalid() {
        var user = createValidUser("John", "Doe", "invalid-email", 2000);
        var mono = userUseCase.saveUser(user);
        StepVerifier.create(mono)
                .expectErrorMatches(t -> t instanceof IllegalArgumentException && MAIL_FORMAT_INVALID.equals(t.getMessage()))
                .verify();
        verifyNoInteractions(userRepository);
    }

    @Test
    void givenSalaryOutOfRange_whenSaveUser_thenErrorSalaryRange() {
        var user1 = createValidUser("John", "Doe", "john@example.com", -1);
        StepVerifier.create(userUseCase.saveUser(user1))
                .expectErrorMatches(t -> t instanceof IllegalArgumentException && SALARY_RANGE.equals(t.getMessage()))
                .verify();

        var user2 = createValidUser("Jane", "Doe", "jane@example.com", Double.MAX_VALUE);
        StepVerifier.create(userUseCase.saveUser(user2))
                .expectErrorMatches(t -> t instanceof IllegalArgumentException && SALARY_RANGE.equals(t.getMessage()))
                .verify();

        verifyNoInteractions(userRepository);
    }

    @Test
    void givenUsers_whenGetAllUsers_thenReturnFlux() {
        var user1 = createValidUser("John", "Doe", "john@example.com", 2000);
        var user2 = createValidUser("Jane", "Doe", "jane@example.com", 2500);
        when(userRepository.getAllUsers()).thenReturn(Flux.just(user1, user2));

        var flux = userUseCase.getAllUsers();

        StepVerifier.create(flux)
                .expectNext(user1)
                .expectNext(user2)
                .verifyComplete();

        verify(userRepository).getAllUsers();
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void givenId_whenDeleteUser_thenRepositoryCalledAndErrorPropagated() {
        var userId = BigInteger.ONE;
        when(userRepository.deleteUser(userId)).thenReturn(Mono.empty());

        var mono = userUseCase.deleteUser(userId);

        StepVerifier.create(mono)
                .expectErrorMatches(t -> t instanceof IllegalArgumentException && DELETE_USER.equals(t.getMessage()))
                .verify();

        verify(userRepository).deleteUser(userId);
        verifyNoMoreInteractions(userRepository);
    }
}
