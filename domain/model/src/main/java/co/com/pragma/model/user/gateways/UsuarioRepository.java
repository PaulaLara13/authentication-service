package co.com.pragma.model.user.gateways;

import co.com.pragma.model.user.Usuario;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigInteger;

public interface UsuarioRepository {

    //Guardar usuario
    Mono<Usuario> guardar(Usuario usuario);

    //Validacion existe correo
    Mono<Boolean> existePorCorreo(String correo);

    Flux<Usuario> getAllUsers();

    Mono<Void> deleteUsuario(BigInteger id);
}
