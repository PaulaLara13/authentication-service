package co.com.pragma.model.user.gateways;

import co.com.pragma.model.user.User;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

public interface UserRepository {

    User saveUser(User user);

    boolean existsByMail(String mail);

    List<User> getAllUsers();

    void deleteUser(BigInteger id);
    
    /**
     * Find a user by email
     * @param email the email to search for
     * @return an Optional containing the user if found, or empty if not found
     */
    Optional<User> findByEmail(String email);
}
