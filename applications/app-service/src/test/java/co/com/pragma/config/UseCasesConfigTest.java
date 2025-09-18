package co.com.pragma.config;

import co.com.pragma.model.user.User;
import co.com.pragma.model.user.gateways.UserRepository;
import co.com.pragma.model.user.gateways.PasswordHasher;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigInteger;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@SpringBootTest
@ActiveProfiles("test")
public class UseCasesConfigTest {

    @Test
    void testUseCaseBeansExist() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(TestConfig.class)) {
            String[] beanNames = context.getBeanDefinitionNames();

            boolean useCaseBeanFound = false;
            for (String beanName : beanNames) {
                if (beanName.endsWith("UseCase")) {
                    useCaseBeanFound = true;
                    break;
                }
            }

            assertTrue(useCaseBeanFound, "No use case beans were found in the application context");
        }
    }

    @Configuration
    @Import(UseCasesConfig.class)
    static class TestConfig {
        
        @Bean
        public UserRepository userRepository() {
            // Create a mock UserRepository
            UserRepository userRepository = Mockito.mock(UserRepository.class);
            
            // Configure the mock to return empty values for all methods (reactive)
            when(userRepository.getAllUsers()).thenReturn(Flux.empty());
            when(userRepository.findByEmail(anyString())).thenReturn(Mono.empty());
            when(userRepository.existsByMail(anyString())).thenReturn(Mono.just(false));
            when(userRepository.saveUser(any(User.class))).thenReturn(Mono.just(new User()));
            when(userRepository.deleteUser(any(BigInteger.class))).thenReturn(Mono.empty());
            
            return userRepository;
        }

        @Bean
        public PasswordHasher passwordHasher() {
            PasswordHasher hasher = Mockito.mock(PasswordHasher.class);
            when(hasher.encode(anyString())).thenAnswer(inv -> inv.getArgument(0));
            when(hasher.matches(anyString(), anyString())).thenReturn(true);
            return hasher;
        }
        
        @Bean
        public MyUseCase myUseCase() {
            return new MyUseCase();
        }
    }

    static class MyUseCase {
        public String execute() {
            return "MyUseCase Test";
        }
    }
}