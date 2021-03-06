package ai.distil.integration.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

import static ai.distil.integration.IntegrationApp.getOverrideArguments;

@Data
@Component
@ConfigurationProperties("ai.distil.integrations.app")
public class AppConfig {

    public static Integer MAX_DATA_SOURCE_SIZE;

    private Integer maxDataSourceSize;

    @PostConstruct
    public void init() {
        if (maxDataSourceSize != null && maxDataSourceSize > 0) {
            MAX_DATA_SOURCE_SIZE = maxDataSourceSize;
        }

        if (getOverrideArguments().getMaxDataSourceSize() > 0) {
            MAX_DATA_SOURCE_SIZE = getOverrideArguments().getMaxDataSourceSize();
        }
    }

}
