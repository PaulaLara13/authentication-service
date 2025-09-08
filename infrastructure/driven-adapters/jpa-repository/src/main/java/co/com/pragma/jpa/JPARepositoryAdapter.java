package co.com.pragma.jpa;

import co.com.pragma.jpa.entity.UserEntity;
import co.com.pragma.jpa.exception.UserNotFoundException;
import co.com.pragma.jpa.helper.AdapterOperations;
import co.com.pragma.model.user.User;
import co.com.pragma.model.user.gateways.UserRepository;
import jakarta.transaction.Transactional;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.math.BigInteger;

@Repository
public class JPARepositoryAdapter extends AdapterOperations<User, UserEntity, BigInteger, JPARepository> implements UserRepository
{

    public JPARepositoryAdapter(JPARepository repository, ObjectMapper mapper) {
        super(repository, mapper, d -> mapper.map(d, User.class));
    }

    @Transactional
    @Override
    public Mono<User> saveUser(User user) {
        return Mono.fromCallable(() -> {
            UserEntity userEntity = repository.save(toData(user));
            return toEntity(userEntity);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<Boolean> existsByMail(String correo) {
        return Mono.fromCallable(() -> repository.existsByMail(correo))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Flux<User> getAllUsers() {
        return Mono.fromCallable(() -> toList(repository.findAll()))
                .flatMapMany(Flux::fromIterable)
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Transactional
    @Override
    public Mono<Void> deleteUser(BigInteger id) {
        return Mono.fromRunnable(() -> {
            UserEntity usuario = repository.findById(id)
                    .orElseThrow(UserNotFoundException::new);
            repository.deleteById(usuario.getId());
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

}
