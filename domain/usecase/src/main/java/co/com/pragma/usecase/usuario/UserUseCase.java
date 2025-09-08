package co.com.pragma.usecase.usuario;

import co.com.pragma.model.user.gateways.UsuarioRepository;
import co.com.pragma.model.user.Usuario;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigInteger;
import java.util.regex.Pattern;

import static co.com.pragma.common.Constants.*;

@RequiredArgsConstructor
public class UserUseCase {
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$");

    private final UsuarioRepository usuarioRepository;

    public Mono<Usuario> saveUser(Usuario usuario) {
        validarCampos(usuario);
        return usuarioRepository.existePorCorreo(usuario.getMail())
                .flatMap(existe -> {
                    if (existe) {
                        return Mono.error(new IllegalArgumentException(MAIL_EXIST));
                    }
                    return usuarioRepository.guardar(usuario);
                });
    }


    private void validarCampos(Usuario usuario) {
        if (usuario.getName() == null || usuario.getName().isBlank()){
            throw new IllegalArgumentException(NAME_VALIDATION);
        }
        if (usuario.getLastname() == null || usuario.getLastname().isBlank()) {
            throw new IllegalArgumentException(LASTNAME_VALIDATION);
        }
        if (usuario.getMail() == null || usuario.getMail().isBlank()){
            throw new IllegalArgumentException(MAIL_VALIDATION);
        }
        if (!EMAIL_PATTERN.matcher(usuario.getMail()).matches()){
            throw new IllegalArgumentException(MAIL_FORMAT_INVALID);
        }
        if (usuario.getSalary() < MIN_RANGE_SALARY || usuario.getSalary() > MAX_RANGE_SALARY){
            throw new IllegalArgumentException(SALARY_RANGE);
        }
    }

    public Flux<Usuario> getAllUsers() {
        return usuarioRepository.getAllUsers();
    }

    public Mono<Void> deleteUsuarioId(BigInteger id) {
        usuarioRepository.deleteUsuario(id);
        throw new IllegalArgumentException(DELETE_USER);
    }

}
