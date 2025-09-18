package co.com.pragma.jpa;

import co.com.pragma.jpa.entity.UserEntity;
import co.com.pragma.model.user.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.QueryByExampleExecutor;
import reactor.core.publisher.Mono;

import java.math.BigInteger;

public interface JPARepository extends CrudRepository<UserEntity, BigInteger>, QueryByExampleExecutor<UserEntity> {

    Mono<Boolean> existsByEmail(String email);

    Mono<UserEntity> findByEmail(String email);
}
