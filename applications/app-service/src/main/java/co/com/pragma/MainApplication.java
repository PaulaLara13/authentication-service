package co.com.pragma;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication(
    scanBasePackages = {
        "co.com.pragma.api",
        "co.com.pragma.config",
        "co.com.pragma.usecase",
        "co.com.pragma.model",
        "co.com.pragma.infrastructure",
        "co.com.pragma.r2dbc"
    }
)
@EnableConfigurationProperties
@ConfigurationPropertiesScan
public class MainApplication {
    public static void main(String[] args) {
        SpringApplication.run(MainApplication.class, args);
    }
}
