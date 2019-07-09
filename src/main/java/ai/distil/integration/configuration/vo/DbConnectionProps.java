package ai.distil.integration.configuration.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DbConnectionProps {
    private String protocol;
    private String props;
}
