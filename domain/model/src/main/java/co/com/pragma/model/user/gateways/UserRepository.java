package co.com.pragma.model.user.gateways;

import co.com.pragma.model.user.User;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigInteger;

public interface UserRepository {

    Mono<User> saveUser(User user);

    Mono<Boolean> existsByMail(String mail);

    Flux<User> getAllUsers();

    Mono<Void> deleteUser(BigInteger id);

    Mono findByMail(String email);
}
