package co.com.pragma.api;

import co.com.pragma.api.dto.CreateUserDto;
import co.com.pragma.api.dto.UserDto;
import co.com.pragma.api.mapper.UserDtoMapper;
import co.com.pragma.model.user.ApiResponse;
import co.com.pragma.usecase.usuario.UserUseCase;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.math.BigInteger;
import java.util.List;

import static co.com.pragma.common.Constants.*;


@RestController
@RequestMapping(value = "/api/v1/user", produces = MediaType.APPLICATION_JSON_VALUE)
@AllArgsConstructor
public class ApiRest {

    private final UserUseCase userUseCase;

    private final UserDtoMapper userMapper;
    private static final Logger log = LoggerFactory.getLogger(ApiRest.class);


    @PostMapping
    public Mono<ResponseEntity<ApiResponse>> createUser(@RequestBody CreateUserDto createUserDto) {
        log.info(START_CREATE_USER, createUserDto);
        return userUseCase.saveUser(userMapper.toModel(createUserDto))
                .map(apiResponse -> ResponseEntity.status(HttpStatus.CREATED).body(apiResponse));
    }


    @GetMapping
    public Mono<ResponseEntity<List<UserDto>>> getAllUser(){
        log.info(START_GET_ALL_USERS);
        return userUseCase.getAllUsers()
                .map(userMapper::toResponse)
                .collectList()
                .map(ResponseEntity::ok);
    }

    @DeleteMapping(path = "/{id}")
    public Mono<ResponseEntity<Void>> deleteUser(@PathVariable(name="id") BigInteger id){
        log.info(START_DELETE, id);
        return userUseCase.deleteUserId(id)
                .thenReturn(ResponseEntity.ok().build());
    }

}
