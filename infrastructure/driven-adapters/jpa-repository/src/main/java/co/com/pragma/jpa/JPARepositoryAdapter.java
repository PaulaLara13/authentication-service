package co.com.pragma.jpa;

import co.com.pragma.jpa.entity.UserEntity;
import co.com.pragma.jpa.exception.UserNotFoundException;
import co.com.pragma.jpa.helper.AdapterOperations;
import co.com.pragma.model.user.gateways.UsuarioRepository;
import co.com.pragma.model.user.Usuario;
import jakarta.transaction.Transactional;
import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.math.BigInteger;

@Repository
public class JPARepositoryAdapter extends AdapterOperations<Usuario, UserEntity, BigInteger, JPARepository> implements UsuarioRepository
{

    public JPARepositoryAdapter(JPARepository repository, ObjectMapper mapper) {
        super(repository, mapper, d -> mapper.map(d, Usuario.class));
    }

    // Guardar usuario
    @Transactional
    @Override
    public Mono<Usuario> guardar(Usuario user) {
        return Mono.fromCallable(() -> {
            UserEntity userEntity = repository.save(toData(user));
            return toEntity(userEntity);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    // Validar si existe correo
    @Override
    public Mono<Boolean> existePorCorreo(String correo) {
        return Mono.fromCallable(() -> repository.existsByMail(correo))
                .subscribeOn(Schedulers.boundedElastic());
    }

    // Obtener todos los usuarios
    @Override
    public Flux<Usuario> getAllUsers() {
        return Mono.fromCallable(() -> toList(repository.findAll()))
                .flatMapMany(Flux::fromIterable)
                .subscribeOn(Schedulers.boundedElastic());
    }

    // Eliminar usuario
    @Transactional
    @Override
    public Mono<Void> deleteUsuario(BigInteger id) {
        return Mono.fromRunnable(() -> {
            UserEntity usuario = repository.findById(id)
                    .orElseThrow(UserNotFoundException::new);
            repository.deleteById(usuario.getId());
        }).subscribeOn(Schedulers.boundedElastic()).then();
}

}
