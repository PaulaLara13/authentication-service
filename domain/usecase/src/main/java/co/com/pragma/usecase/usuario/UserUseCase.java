package co.com.pragma.usecase.usuario;

import co.com.pragma.model.user.ApiResponse;
import co.com.pragma.model.user.User;
import co.com.pragma.model.user.gateways.UserRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.math.BigInteger;
import java.util.regex.Pattern;
import static co.com.pragma.common.Constants.*;
import static co.com.pragma.common.Constants.REQUESTS_CREATEID;
import static co.com.pragma.common.Constants.REQUESTS_CREATE_NOTID;


@RequiredArgsConstructor
public class UserUseCase {
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$");

    private final UserRepository userRepository;

    public Mono<ApiResponse> saveUser(User user) {
        ValidateFields(user);
        return userRepository.existsByMail(user.getMail())
                .flatMap(existe -> {
                    if (existe) {
                        return Mono.error(new IllegalArgumentException(MAIL_EXIST));
                    }
                    return userRepository.saveUser(user)
                            .map(saved ->{
                                String mensaje = (saved.getId() != null)
                                        ? REQUESTS_CREATEID + saved.getId()
                                        : REQUESTS_CREATE_NOTID;
                                return new ApiResponse(mensaje);
                            });
                });
    }


    private void ValidateFields(User user) {
        if (user.getName() == null || user.getName().isBlank()){
            throw new IllegalArgumentException(NAME_VALIDATION);
        }
        if (user.getLastname() == null || user.getLastname().isBlank()) {
            throw new IllegalArgumentException(LASTNAME_VALIDATION);
        }
        if (user.getMail() == null || user.getMail().isBlank()){
            throw new IllegalArgumentException(MAIL_VALIDATION);
        }
        if (!EMAIL_PATTERN.matcher(user.getMail()).matches()){
            throw new IllegalArgumentException(MAIL_FORMAT_INVALID);
        }
        if (user.getSalary() < MIN_RANGE_SALARY || user.getSalary() > MAX_RANGE_SALARY){
            throw new IllegalArgumentException(SALARY_RANGE);
        }
    }

    public Flux<User> getAllUsers() {
        return userRepository.getAllUsers();
    }

    public Mono<Void> deleteUserId(BigInteger id) {
        userRepository.deleteUser(id);
        throw new IllegalArgumentException(DELETE_USER);
    }

}
