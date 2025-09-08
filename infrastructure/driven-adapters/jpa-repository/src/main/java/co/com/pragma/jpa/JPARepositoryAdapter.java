package co.com.pragma.jpa;

import co.com.pragma.jpa.entity.RoleEntity;
import co.com.pragma.jpa.entity.UserEntity;
import co.com.pragma.jpa.exception.UserNotFoundException;
import co.com.pragma.jpa.helper.AdapterOperations;
import co.com.pragma.model.user.Role;
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
    private final RoleJPARepository roleRepo;


    public JPARepositoryAdapter(JPARepository repository, RoleJPARepository roleRepo, ObjectMapper mapper) {
        super(repository, mapper, d -> null); // no usar map automático, haremos manual
        this.roleRepo = roleRepo;
    }

    private User toDomainUser(UserEntity e) {
        if (e == null) return null;
        var u = new User();
        u.setId(e.getId());
        u.setName(e.getName());
        u.setLastname(e.getLastname());
        u.setMail(e.getMail());
        u.setSalary(e.getSalary());
        u.setPasswordHash(e.getPasswordHash());
        var r = new Role();
        r.setId(e.getRole().getId());
        r.setName(e.getRole().getName());
        u.setRole(r);
        return u;
    }

    @Transactional
    @Override
    public Mono<User> saveUser(User user) {
        return Mono.fromCallable(() -> {
            RoleEntity role = roleRepo.findById(user.getRole().getId())
                    .orElseThrow(() -> new IllegalArgumentException("Rol no existe"));
            UserEntity e = new UserEntity();
            e.setId(user.getId());
            e.setName(user.getName());
            e.setLastname(user.getLastname());
            e.setMail(user.getMail());
            e.setSalary(user.getSalary());
            e.setPasswordHash(user.getPasswordHash());
            e.setRole(role);
            return toDomainUser(repository.save(e));
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<Boolean> existsByMail(String correo) {
        return Mono.fromCallable(() -> repository.existsByMail(correo))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Flux<User> getAllUsers() {
        return Mono.fromCallable(() -> (Iterable<UserEntity>) repository.findAll())
                .flatMapMany(Flux::fromIterable)
                .map(this::toDomainUser)
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Transactional
    @Override
    public Mono<Void> deleteUser(BigInteger id) {
        return Mono.fromRunnable(() -> {
            UserEntity user = repository.findById(id)
                    .orElseThrow(co.com.pragma.jpa.exception.UserNotFoundException::new);
            repository.deleteById(user.getId());
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    @Override
    public Mono<User> findByMail(String email) {
        return Mono.fromCallable(() -> repository.findByMail(email).map(this::toDomainUser).orElse(null))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(u -> u == null ? Mono.empty() : Mono.just(u));
    }
}
