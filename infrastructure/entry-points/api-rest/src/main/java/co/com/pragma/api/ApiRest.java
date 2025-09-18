package co.com.pragma.api;

import static co.com.pragma.common.Constants.*;
import co.com.pragma.api.dto.CreateUserDto;
import co.com.pragma.api.dto.UserDto;
import co.com.pragma.api.mapper.UserDtoMapper;
import co.com.pragma.model.user.ApiResponse;
import co.com.pragma.model.user.Role;
import co.com.pragma.model.user.RoleName;
import co.com.pragma.model.user.gateways.PasswordHasher;
import co.com.pragma.usecase.usuario.UserUseCase;
import java.math.BigInteger;
import java.util.HashSet;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping(value = "/api/v1/user", produces = MediaType.APPLICATION_JSON_VALUE)
@AllArgsConstructor
public class ApiRest {

    private final UserUseCase userUseCase;
    private final UserDtoMapper userMapper;
    private final PasswordHasher passwordHasher;
    private static final Logger log = LoggerFactory.getLogger(ApiRest.class);

    @PostMapping
    public Mono<ResponseEntity<ApiResponse>> createUser(@RequestBody CreateUserDto createUserDto) {
        log.info(START_CREATE_USER, createUserDto);
        var user = userMapper.toModel(createUserDto);
        if (createUserDto.password() != null) {
            user.setPassword(passwordHasher.encode(createUserDto.password()));
        }
        if (createUserDto.role() != null && !createUserDto.role().isBlank()) {
            var raw = createUserDto.role().trim().toUpperCase();
            var enumName = raw.startsWith(ROLE_) ? raw : ROLE_ + raw;
            var role = Role.from(RoleName.valueOf(enumName));
            var roles = new HashSet<Role>();
            roles.add(role);
            user.setRoles(roles);
        }
        return userUseCase.saveUser(user)
                .map(apiResponse -> ResponseEntity.status(HttpStatus.CREATED).body(apiResponse));
    }

    @GetMapping
    public Flux<UserDto> getAllUsers(){
        log.info(START_GET_ALL_USERS);
        return userUseCase.getAllUsers()
                .map(userMapper::toResponse);
    }

    @DeleteMapping(path = "/{id}")
    public Mono<ResponseEntity<Void>> deleteUser(@PathVariable(name="id") BigInteger id){
        log.info(START_DELETE, id);
        return userUseCase.deleteUser(id)
                .thenReturn(ResponseEntity.ok().build());
    }

}
