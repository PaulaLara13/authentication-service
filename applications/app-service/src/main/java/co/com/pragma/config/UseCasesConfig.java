package co.com.pragma.config;

import co.com.pragma.model.user.gateways.UsuarioRepository;
import co.com.pragma.usecase.usuario.UserUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;

@Configuration
@ComponentScan(basePackages = "co.com.pragma.usecase",
        includeFilters = {
                @ComponentScan.Filter(type = FilterType.REGEX, pattern = "^.+UseCase$")
        },
        useDefaultFilters = false)
public class UseCasesConfig {
    @Bean
    public UserUseCase userUseCase(UsuarioRepository usuarioRepository) {
        return new UserUseCase(usuarioRepository);
    }
}
