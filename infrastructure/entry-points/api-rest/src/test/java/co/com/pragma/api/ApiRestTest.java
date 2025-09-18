package co.com.pragma.api;

import co.com.pragma.api.dto.CreateUserDto;
import co.com.pragma.api.dto.UserDto;
import co.com.pragma.api.mapper.UserDtoMapper;
import co.com.pragma.model.user.ApiResponse;
import co.com.pragma.model.user.User;
import co.com.pragma.usecase.usuario.UserUseCase;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.math.BigInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@ExtendWith(MockitoExtension.class)

class ApiRestTest {
    @Mock
    private UserUseCase userUseCase;

    @Mock
    private UserDtoMapper userMapper;

    @InjectMocks
    private ApiRest apiRest;

    @Test
    void createUser_shouldReturn201WithBody() {
        // Arrange
        CreateUserDto createUserDto = mock(CreateUserDto.class);
        User user = mock(User.class);
        ApiResponse apiResponse = new ApiResponse("User created successfully");

        when(userMapper.toModel(createUserDto)).thenReturn(user);
        when(userUseCase.saveUser(user)).thenReturn(Mono.just(apiResponse));

        // Act
        ResponseEntity<ApiResponse> response = apiRest.createUser(createUserDto).block();

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isSameAs(apiResponse);

        // Verify
        verify(userMapper).toModel(createUserDto);
        verify(userUseCase).saveUser(user);
        verifyNoMoreInteractions(userUseCase, userMapper);
    }

    @Test
    void getAllUsers_shouldReturn200WithUserList() {
        // Arrange
        User user1 = mock(User.class);
        User user2 = mock(User.class);
        UserDto dto1 = mock(UserDto.class);
        UserDto dto2 = mock(UserDto.class);

        when(userUseCase.getAllUsers()).thenReturn(Flux.just(user1, user2));
        when(userMapper.toResponse(user1)).thenReturn(dto1);
        when(userMapper.toResponse(user2)).thenReturn(dto2);

        // Act
        var list = apiRest.getAllUsers().collectList().block();

        // Assert
        assertThat(list).containsExactly(dto1, dto2);

        // Verify
        verify(userUseCase).getAllUsers();
        verify(userMapper).toResponse(user1);
        verify(userMapper).toResponse(user2);
        verifyNoMoreInteractions(userUseCase, userMapper);
    }

    @Test
    void getAllUsers_whenNoUsers_shouldReturnEmptyList() {
        // Arrange
        when(userUseCase.getAllUsers()).thenReturn(Flux.empty());

        // Act
        var list = apiRest.getAllUsers().collectList().block();

        // Assert
        assertThat(list).isEmpty();

        // Verify
        verify(userUseCase).getAllUsers();
        verifyNoMoreInteractions(userUseCase);
        verifyNoInteractions(userMapper);
    }

    @Test
    void deleteUser_shouldCallUseCaseAndReturn200() {
        // Arrange
        BigInteger id = BigInteger.valueOf(123L);
        when(userUseCase.deleteUser(id)).thenReturn(Mono.empty());

        // Act
        ResponseEntity<Void> response = apiRest.deleteUser(id).block();

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNull();

        // Verify correct ID was used
        ArgumentCaptor<BigInteger> idCaptor = ArgumentCaptor.forClass(BigInteger.class);
        verify(userUseCase).deleteUser(idCaptor.capture());
        assertThat(idCaptor.getValue()).isEqualTo(id);

        verifyNoMoreInteractions(userUseCase);
        verifyNoInteractions(userMapper);
    }
}
