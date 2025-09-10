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
    public User saveUser(User user) {
        UserEntity userEntity = toData(user);
        if (user.getRoles() != null && !user.getRoles().isEmpty()) {
            var roleEntities = new HashSet<co.com.pragma.jpa.entity.RoleEntity>();
            for (Role r : user.getRoles()) {
                if (r.getName() == null) continue;
                var raw = r.getName().name(); // e.g., ROLE_ADMIN
                var dbName = raw.startsWith("ROLE_") ? raw.substring(5) : raw; // ADMIN
                var roleEntity = roleRepository.findByName(dbName);
                if (roleEntity != null) {
                    roleEntities.add(roleEntity);
                }
            }
            userEntity.setRoles(roleEntities);
        }
        userEntity = repository.save(userEntity);
        return toEntity(userEntity);
    }

    @Override
    public boolean existsByMail(String correo) {
        return repository.existsByEmail(correo);
    }

    @Override
    public List<User> getAllUsers() {
        return toList(repository.findAll()).stream()
                .map(this::attachRoles)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public void deleteUser(BigInteger id) {
        if (!repository.existsById(id)) {
            throw new UserNotFoundException("User not found with id: " + id);
        }
        repository.deleteById(id);
    }
    
    @Override
    public Optional<User> findByEmail(String email) {
        UserEntity userEntity = repository.findByEmail(email);
        if (userEntity == null) {
            return Optional.empty();
        }
        return Optional.of(attachRoles(toEntity(userEntity)));
    }

    private User attachRoles(User user) {
        try {
            var entity = repository.findByEmail(user.getEmail());
            if (entity != null && entity.getRoles() != null) {
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
        } catch (Exception e) {
            return user;
        }
    }
}
