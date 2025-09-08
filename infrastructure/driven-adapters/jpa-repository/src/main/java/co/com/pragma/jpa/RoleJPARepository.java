package co.com.pragma.jpa;

import co.com.pragma.jpa.entity.RoleEntity;
import org.springframework.data.repository.CrudRepository;

import java.math.BigInteger;
import java.util.Optional;

public interface RoleJPARepository extends CrudRepository<RoleEntity, BigInteger> {
    Optional<RoleEntity> findByName(String name);
}