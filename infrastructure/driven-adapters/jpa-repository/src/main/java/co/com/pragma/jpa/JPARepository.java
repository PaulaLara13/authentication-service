package co.com.pragma.jpa;

import co.com.pragma.jpa.entity.UserEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.QueryByExampleExecutor;

import java.math.BigInteger;

public interface JPARepository extends CrudRepository<UserEntity, BigInteger>, QueryByExampleExecutor<UserEntity> {

    boolean existsByMail(String correo);

}
