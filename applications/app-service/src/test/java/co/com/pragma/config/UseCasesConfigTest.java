package co.com.pragma.config;

import co.com.pragma.model.user.User;
import co.com.pragma.model.user.gateways.UserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigInteger;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

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
            
            // Configure the mock to return empty values for all methods
            when(userRepository.getAllUsers()).thenReturn(Collections.emptyList());
            when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
            when(userRepository.existsByMail(anyString())).thenReturn(false);
            when(userRepository.saveUser(any(User.class))).thenReturn(new User());
            // deleteUser is a void method, so we use doNothing()
            Mockito.doNothing().when(userRepository).deleteUser(any(BigInteger.class));
            
            return userRepository;
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