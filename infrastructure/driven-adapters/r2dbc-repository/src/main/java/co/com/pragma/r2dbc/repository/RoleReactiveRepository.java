package co.com.pragma.r2dbc.repository;

import co.com.pragma.r2dbc.entity.RoleEntity;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.math.BigInteger;

public interface RoleReactiveRepository extends ReactiveCrudRepository<RoleEntity, BigInteger> {
    Mono<RoleEntity> findByName(String name);
}
