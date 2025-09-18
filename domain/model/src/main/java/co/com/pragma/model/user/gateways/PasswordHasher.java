package co.com.pragma.model.user.gateways;

public interface PasswordHasher {
    String encode(String rawPassword);
    boolean matches(String rawPassword, String encodedPassword);
}
