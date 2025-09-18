package co.com.pragma.r2dbc;

import co.com.pragma.model.user.Role;
import co.com.pragma.model.user.RoleName;
import co.com.pragma.model.user.User;
import co.com.pragma.model.user.gateways.UserRepository;
import co.com.pragma.r2dbc.entity.RoleEntity;
import co.com.pragma.r2dbc.entity.UserEntity;
import co.com.pragma.r2dbc.repository.RoleReactiveRepository;
import co.com.pragma.r2dbc.repository.UserReactiveRepository;
import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public class R2dbcRepositoryAdapter implements UserRepository {

    private static final Logger log = LoggerFactory.getLogger(R2dbcRepositoryAdapter.class);

    private final UserReactiveRepository userRepository;
    private final RoleReactiveRepository roleRepository;
    private final DatabaseClient databaseClient;

    public R2dbcRepositoryAdapter(UserReactiveRepository userRepository,
                                  RoleReactiveRepository roleRepository,
                                  DatabaseClient databaseClient) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.databaseClient = databaseClient;
    }

    @Override
    public Mono<User> saveUser(User user) {
        UserEntity ue = toData(user);
        return userRepository.save(ue)
                .flatMap(saved -> {
                    Long savedId = saved.getId();
                    Mono<Long> clear = databaseClient.sql("DELETE FROM user_roles WHERE user_id = :uid")
                            .bind("uid", savedId)
                            .fetch().rowsUpdated();

                    Mono<Integer> insertRelations = (user.getRoles() == null || user.getRoles().isEmpty())
                            ? Mono.just(0)
                            : Flux.fromIterable(user.getRoles())
                                .flatMap(r -> {
                                    String raw = r.getName() != null ? r.getName().name() : "";
                                    String dbName = raw.startsWith("ROLE_") ? raw.substring(5) : raw;
                                    return roleRepository.findByName(dbName)
                                            .flatMap(re -> databaseClient.sql("INSERT INTO user_roles(user_id, role_id) VALUES(:uid, :rid)")
                                                    .bind("uid", savedId)
                                                    .bind("rid", re.getId())
                                                    .fetch().rowsUpdated());
                                })
                                .then(Mono.just(1));

                    return clear.then(insertRelations)
                            .then(attachRolesReactive(toDomain(saved)));
                });
    }

    @Override
    public Mono<Boolean> existsByMail(String mail) {
        return userRepository.existsByEmail(mail)
                .map(exists -> exists != null && exists);
    }

    @Override
    public Flux<User> getAllUsers() {
        return userRepository.findAll()
                .flatMap(ue -> attachRolesReactive(toDomain(ue)));
    }

    @Override
    public Mono<Void> deleteUser(BigInteger id) {
        Long uid = id != null ? id.longValue() : null;
        return databaseClient.sql("DELETE FROM user_roles WHERE user_id = :uid")
                .bind("uid", uid)
                .fetch().rowsUpdated()
                .then(userRepository.deleteById(uid));
    }
    @Override
    public Mono<Boolean> existsById(BigInteger id) {
        return userRepository.existsById(id.longValue()).map(exists -> exists != null && exists);
    }

    @Override
    public Mono<User> findByEmail(String email) {
        log.info("[R2DBC] findByEmail email={}", email);
        return userRepository.findByEmail(email)
                .map(this::toDomain)
                .flatMap(u -> {
                    log.info("[R2DBC] user loaded id={} (class={})", u.getId(), u.getId() != null ? u.getId().getClass().getName() : null);
                    return attachRolesReactive(u);
                });
    }

    private Mono<User> attachRolesReactive(User user) {
        if (user.getId() == null) return Mono.just(user);
        return queryRolesByUserId(user.getId())
                .map(this::toDomainRole)
                .collectList()
                .map(list -> {
                    Set<Role> roles = new HashSet<>(list);
                    user.setRoles(roles);
                    return user;
                });
    }

    private Flux<RoleEntity> queryRolesByUserId(BigInteger userId) {
        log.info("[R2DBC] queryRolesByUserId uid={} (class={})", userId, userId != null ? userId.getClass().getName() : null);
        String sql = "SELECT r.id as id, r.name as name FROM RoleEntity r " +
                "INNER JOIN user_roles ur ON ur.role_id = r.id WHERE ur.user_id = :uid";
        return databaseClient.sql(sql)
                .bind("uid", userId != null ? userId.longValue() : null)
                .map((row, meta) -> {
                    RoleEntity re = new RoleEntity();
                    Object idVal = row.get("id");
                    if (idVal instanceof Number) {
                        re.setId(BigInteger.valueOf(((Number) idVal).longValue()));
                    } else if (idVal instanceof BigInteger) {
                        re.setId((BigInteger) idVal);
                    }
                    Object nameVal = row.get("name");
                    re.setName(nameVal != null ? nameVal.toString() : null);
                    return re;
                })
                .all();
    }

    private UserEntity toData(User user) {
        UserEntity ue = new UserEntity();
        ue.setId(user.getId() != null ? user.getId().longValue() : null);
        ue.setName(user.getName());
        ue.setLastname(user.getLastname());
        ue.setDate(user.getDateOfBirth());
        ue.setAddress(user.getAddress());
        ue.setPhone(user.getPhone() != null ? Integer.valueOf(user.getPhone()) : null);
        ue.setEmail(user.getEmail());
        ue.setSalary(user.getSalary() != null ? user.getSalary() : 0.0);
        ue.setPassword(user.getPassword());
        return ue;
    }

    private User toDomain(UserEntity ue) {
        User u = new User();
        u.setId(ue.getId() != null ? BigInteger.valueOf(ue.getId()) : null);
        u.setName(ue.getName());
        u.setLastname(ue.getLastname());
        u.setDateOfBirth(ue.getDate());
        u.setAddress(ue.getAddress());
        u.setPhone(ue.getPhone() != null ? ue.getPhone().toString() : null);
        u.setEmail(ue.getEmail());
        u.setSalary(ue.getSalary());
        u.setPassword(ue.getPassword());
        u.setEnabled(true);
        u.setAccountNonExpired(true);
        u.setCredentialsNonExpired(true);
        u.setAccountNonLocked(true);
        u.setRoles(new HashSet<>());
        return u;
    }

    private Role toDomainRole(RoleEntity re) {
        String raw = re.getName() != null ? re.getName().trim().toUpperCase() : "";
        String enumName = raw.startsWith("ROLE_") ? raw : "ROLE_" + raw;
        return Role.from(RoleName.valueOf(enumName));
    }
}
