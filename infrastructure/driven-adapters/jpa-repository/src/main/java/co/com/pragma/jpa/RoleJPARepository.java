package co.com.pragma.jpa;

import co.com.pragma.jpa.entity.RoleEntity;
import org.springframework.data.repository.CrudRepository;
import reactor.core.publisher.Mono;

import java.math.BigInteger;

public interface RoleJpaRepository extends CrudRepository<RoleEntity, BigInteger> {
    Mono<RoleEntity> findByName(String name);
}
