package co.com.pragma.jpa;

import co.com.pragma.jpa.entity.RoleEntity;
import org.springframework.data.repository.CrudRepository;

import java.math.BigInteger;

public interface RoleJpaRepository extends CrudRepository<RoleEntity, BigInteger> {
    RoleEntity findByName(String name);
}
