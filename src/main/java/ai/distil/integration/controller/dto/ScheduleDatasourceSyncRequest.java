package ai.distil.integration.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleDatasourceSyncRequest {
    @NotNull(message = "Org Id must be set")
    private Long orgId;
    @NotNull(message = "Connection id must be set")
    private Long connectionId;
    @NotNull(message = "DataSource id must be set")
    private Long dataSourceId;
}
