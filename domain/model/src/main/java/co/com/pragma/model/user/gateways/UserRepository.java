package co.com.pragma.model.user.gateways;

import co.com.pragma.model.user.User;

import java.math.BigInteger;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UserRepository {

    Mono<User> saveUser(User user);

    Mono<Boolean> existsByMail(String mail);

    Mono<Boolean> existsById(BigInteger id);

    Flux<User> getAllUsers();

    Mono<Void> deleteUser(BigInteger id);

    Mono<User> findByEmail(String email);
}
