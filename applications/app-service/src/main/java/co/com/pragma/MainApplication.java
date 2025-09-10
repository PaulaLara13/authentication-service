package co.com.pragma;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(
    scanBasePackages = {
        "co.com.pragma.api",
        "co.com.pragma.config",
        "co.com.pragma.usecase",
        "co.com.pragma.model",
        "co.com.pragma.infrastructure",
        "co.com.pragma.jpa"
    }
)
@EnableJpaRepositories(basePackages = {
    "co.com.pragma.jpa"
})
@EntityScan(basePackages = {
    "co.com.pragma.jpa.entity",
    "co.com.pragma.model"
})
@EnableConfigurationProperties
@ConfigurationPropertiesScan
public class MainApplication {
    public static void main(String[] args) {
        SpringApplication.run(MainApplication.class, args);
    }
}
