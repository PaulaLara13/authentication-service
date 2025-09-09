package co.com.pragma.jpa;

import co.com.pragma.jpa.entity.RoleEntity;
import co.com.pragma.model.user.Role;
import co.com.pragma.model.user.gateways.RoleRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.math.BigInteger;
@Repository
public class RoleRepositoryAdapter implements RoleRepository {

    private final RoleJPARepository roleRepo;

    public RoleRepositoryAdapter(RoleJPARepository roleRepo) {
        this.roleRepo = roleRepo;
    }

    private Role toDomain(RoleEntity e) {
        if (e == null) return null;
        var r = new Role();
        r.setId(e.getId());
        r.setName(e.getName());
        return r;
    }

    @Override
    public Mono<Role> findById(BigInteger id) {
        return Mono.fromCallable(() -> roleRepo.findById(id).map(this::toDomain).orElse(null))
                .flatMap(r -> r == null ? Mono.empty() : Mono.just(r))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<Role> findByName(String name) {
        return Mono.fromCallable(() -> roleRepo.findByName(name).map(this::toDomain).orElse(null))
                .flatMap(r -> r == null ? Mono.empty() : Mono.just(r))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Flux<Role> findAll() {
        return Mono.fromCallable(() -> (Iterable<RoleEntity>) roleRepo.findAll())
                .flatMapMany(Flux::fromIterable)
                .map(this::toDomain)
                .subscribeOn(Schedulers.boundedElastic());
    }

}
