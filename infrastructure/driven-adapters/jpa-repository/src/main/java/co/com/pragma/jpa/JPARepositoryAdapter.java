package co.com.pragma.jpa;

import co.com.pragma.jpa.entity.UserEntity;
import co.com.pragma.jpa.exception.UserNotFoundException;
import co.com.pragma.jpa.helper.AdapterOperations;
import co.com.pragma.model.user.User;
import co.com.pragma.model.user.gateways.UserRepository;
import co.com.pragma.model.user.Role;
import co.com.pragma.model.user.RoleName;
import jakarta.transaction.Transactional;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class JPARepositoryAdapter extends AdapterOperations<User, UserEntity, BigInteger, JPARepository> implements UserRepository {

    private final RoleJpaRepository roleRepository;

    public JPARepositoryAdapter(JPARepository repository, RoleJpaRepository roleRepository, ObjectMapper mapper) {
        super(repository, mapper, d -> mapper.map(d, User.class));
        this.roleRepository = roleRepository;
    }

    @Transactional
    @Override
    public Mono<User> saveUser(User user) {
        UserEntity userEntity = toData(user);

        // Si no hay roles, guardar directamente
        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            return Mono.fromCallable(() -> repository.save(userEntity)) // Llama método bloqueante en hilo aparte
                    .subscribeOn(Schedulers.boundedElastic())           // Ejecuta en thread pool separado
                    .map(this::toEntity);
        }

        return Flux.fromIterable(user.getRoles())
                .filter(role -> role.getName() != null)
                .flatMap(role -> {
                    String raw = role.getName().name();  // e.g., ROLE_ADMIN
                    String dbName = raw.startsWith("ROLE_") ? raw.substring(5) : raw;
                    return roleRepository.findByName(dbName); // Mono<RoleEntity>
                })
                .collect(Collectors.toSet()) // ✅ aquí corregido
                .flatMap(roleEntities -> {
                    userEntity.setRoles(roleEntities);  // ahora sí es Set<RoleEntity>
                    return Mono.fromCallable(() -> repository.save(userEntity))
                            .subscribeOn(Schedulers.boundedElastic());
                })
                .map(this::toEntity);

    }

    @Override
    public Mono<Boolean> existsByMail(String correo) {
        return repository.existsByEmail(correo);
    }

    public Flux<User> getAllUsers() {
        return Mono.fromCallable(() -> (List<UserEntity>) repository.findAll()) // bloqueante → Mono<List<UserEntity>>
                .subscribeOn(Schedulers.boundedElastic()) // evitar bloquear el event-loop
                .flatMapMany(entities -> Flux.fromIterable(toList(entities)))   // convertir a Flux<User>
                .flatMap(this::attachRoles);                                    // attachRoles devuelve Mono<User>
    }

    @Transactional
    @Override
    public Mono<Void> deleteUser(BigInteger id) {
        return Mono.fromCallable(() -> repository.existsById(id))
                .subscribeOn(Schedulers.boundedElastic())  // Para no bloquear el hilo reactor
                .flatMap(exists -> {
                    if (!exists) {
                        return Mono.error(new UserNotFoundException("User not found with id: " + id));
                    }
                    // deleteById es bloqueante, envolver también:
                    return Mono.fromRunnable(() -> repository.deleteById(id))
                            .subscribeOn(Schedulers.boundedElastic())
                            .then();
                });
    }
    
    @Override
    public Mono<User> findByEmail(String email) {
        return repository.findByEmail(email) // ya devuelve Mono<UserEntity>
                .flatMap(userEntity -> {
                    if (userEntity == null) {
                        return Mono.empty();
                    }
                    return attachRoles(toEntity(userEntity));
                });
    }

    private Mono<User> attachRoles(User user) {
        return repository.findByEmail(user.getEmail()) // aquí debe retornar un Mono<UserEntity>
                .defaultIfEmpty(new UserEntity()) // si no encuentra nada, devolvemos vacío
                .map(entity -> {
                    if (entity.getRoles() != null) {
                        var mapped = entity.getRoles().stream()
                                .filter(r -> r.getName() != null)
                                .map(r -> {
                                    var raw = r.getName().trim().toUpperCase();
                                    var enumName = raw.startsWith("ROLE_") ? raw : "ROLE_" + raw;
                                    return Role.from(RoleName.valueOf(enumName));
                                })
                                .collect(Collectors.toSet());
                        user.setRoles(mapped);
                    }
                    return user;
                })
                .onErrorReturn(user); // en caso de excepción, devolvemos el user sin cambios
    }
}
