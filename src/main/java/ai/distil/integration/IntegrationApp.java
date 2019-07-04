package ai.distil.integration;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@EnableConfigurationProperties
@EnableFeignClients(basePackages = {"ai.distil.api.internal.proxy"})
@SpringBootApplication(scanBasePackages = {"ai.distil.integration"}, exclude = SecurityAutoConfiguration.class)
public class IntegrationApp {
    public static void main(String[] args) {
        SpringApplication.run(IntegrationApp.class, args);
    }
}
