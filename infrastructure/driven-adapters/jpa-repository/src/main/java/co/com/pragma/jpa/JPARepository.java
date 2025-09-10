package co.com.pragma.jpa;

import co.com.pragma.jpa.entity.UserEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.QueryByExampleExecutor;

import java.math.BigInteger;

public interface JPARepository extends CrudRepository<UserEntity, BigInteger>, QueryByExampleExecutor<UserEntity> {

    boolean existsByEmail(String email);
    
    /**
     * Find a user by their email address
     * @param mail the email address to search for
     * @return the UserEntity if found, or null if not found
     */
    UserEntity findByEmail(String email);
}
