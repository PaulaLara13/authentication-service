package co.com.pragma.usecase.usuario;

import static co.com.pragma.common.Constants.*;

import co.com.pragma.model.user.ApiResponse;
import co.com.pragma.model.user.User;
import co.com.pragma.model.user.gateways.UserRepository;
import java.math.BigInteger;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class UserUseCase {
  private static final Pattern EMAIL_PATTERN =
      Pattern.compile("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$");

  private final UserRepository userRepository;

  public Mono<ApiResponse> saveUser(User user) {
    return Mono.defer(() -> {
      validateFields(user);
      return userRepository
          .existsByMail(user.getMail())
          .flatMap(
              exists -> {
                if (Boolean.TRUE.equals(exists)) {
                  return Mono.error(new IllegalArgumentException(MAIL_EXIST));
                }
                return userRepository.saveUser(user);
              })
          .map(
              savedUser -> {
                String message =
                    (savedUser.getId() != null)
                        ? REQUESTS_CREATEID + savedUser.getId()
                        : REQUESTS_CREATE_NOTID;
                return new ApiResponse(message);
              });
    });
  }

  private void validateFields(User user) {
    if (user.getName() == null || user.getName().isBlank()) {
      throw new IllegalArgumentException(NAME_VALIDATION);
    }
    if (user.getLastname() == null || user.getLastname().isBlank()) {
      throw new IllegalArgumentException(LASTNAME_VALIDATION);
    }
    if (user.getMail() == null || user.getMail().isBlank()) {
      throw new IllegalArgumentException(MAIL_VALIDATION);
    }
    if (!EMAIL_PATTERN.matcher(user.getMail()).matches()) {
      throw new IllegalArgumentException(MAIL_FORMAT_INVALID);
    }
    if (user.getSalary() < MIN_RANGE_SALARY || user.getSalary() > MAX_RANGE_SALARY) {
      throw new IllegalArgumentException(SALARY_RANGE);
    }
  }

  public Flux<User> getAllUsers() {
    return userRepository.getAllUsers();
  }

    public Mono<Void> deleteUser(BigInteger id) {
        return userRepository
                .existsById(id)
                .flatMap(
                        exists ->
                                exists
                                        ? userRepository
                                        .deleteUser(id)
                                        .then(Mono.error(new IllegalArgumentException(DELETE_USER)))
                                        : Mono.error(new IllegalArgumentException(NOT_EXIST_USER + id)));
    }
}
