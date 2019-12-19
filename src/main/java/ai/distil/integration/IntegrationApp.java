package ai.distil.integration;

import com.beust.jcommander.JCommander;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Slf4j
@EnableJpaAuditing
@EnableConfigurationProperties
@EnableFeignClients(basePackages = {"ai.distil.api.internal.proxy", "ai.distil.processing.datapipeline.controller.proxy"})
@SpringBootApplication(scanBasePackages = {"ai.distil.integration", "ai.distil.processing.datapipeline.controller.proxy"}, exclude = SecurityAutoConfiguration.class)
public class IntegrationApp {

    private static OverrideArguments overrideArguments;

    public static OverrideArguments getOverrideArguments() {
//        need it for unit tests
        return overrideArguments == null ? new OverrideArguments() : overrideArguments;
    }

    public static void main(String[] args) {

        //JCommander is a 3rd party tool to parse command line args
        overrideArguments = new OverrideArguments();

        try {
            //populate the argument class from the command line args
            JCommander commander = JCommander.newBuilder()
                    .addObject(overrideArguments)
                    .build();

            commander.parse(args);

            if (overrideArguments.isHelpMode()) {
                //Show help doc and exit
                commander.usage();
                System.exit(0);
            }
        } catch (Exception e) {
            log.error("Error trying to parse command line args: {}", e.getMessage(), e);
            System.exit(1);
        }

        SpringApplication.run(IntegrationApp.class, args);
    }
}
