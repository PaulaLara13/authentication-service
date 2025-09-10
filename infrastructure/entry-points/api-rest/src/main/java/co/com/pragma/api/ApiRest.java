package co.com.pragma.api;

import co.com.pragma.api.dto.CreateUserDto;
import co.com.pragma.api.dto.UserDto;
import co.com.pragma.api.mapper.UserDtoMapper;
import co.com.pragma.model.user.ApiResponse;
import co.com.pragma.model.user.Role;
import co.com.pragma.model.user.RoleName;
import co.com.pragma.usecase.usuario.UserUseCase;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

import static co.com.pragma.common.Constants.*;

@RestController
@RequestMapping(value = "/api/v1/user", produces = MediaType.APPLICATION_JSON_VALUE)
@AllArgsConstructor
public class ApiRest {

    private final UserUseCase userUseCase;

    private final UserDtoMapper userMapper;
    private static final Logger log = LoggerFactory.getLogger(ApiRest.class);


    @PostMapping
    public ResponseEntity<ApiResponse> createUser(@RequestBody CreateUserDto createUserDto) {
        log.info(START_CREATE_USER, createUserDto);
        var user = userMapper.toModel(createUserDto);
        if (createUserDto.password() != null) {
            user.setPassword(createUserDto.password());
        }
        if (createUserDto.role() != null && !createUserDto.role().isBlank()) {
            var raw = createUserDto.role().trim().toUpperCase();
            var enumName = raw.startsWith("ROLE_") ? raw : "ROLE_" + raw;
            var role = Role.from(RoleName.valueOf(enumName));
            var roles = new HashSet<Role>();
            roles.add(role);
            user.setRoles(roles);
        }

        ApiResponse apiResponse = userUseCase.saveUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    }


    @GetMapping
    public ResponseEntity<List<UserDto>> getAllUsers(){
        log.info(START_GET_ALL_USERS);
        List<UserDto> users = userUseCase.getAllUsers().stream()
                .map(userMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

    @DeleteMapping(path = "/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable(name="id") BigInteger id){
        log.info(START_DELETE, id);
        userUseCase.deleteUser(id);
        return ResponseEntity.ok().build();
    }

}
