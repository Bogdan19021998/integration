package ai.distil.integration.controller.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ScheduleDatasourceSyncRequest extends CommonConnectionRequest {
    @NotNull(message = "DataSource id must be set")
    private Long dataSourceId;

    public ScheduleDatasourceSyncRequest(Long orgId, String tenantId, Long connectionId, Long dataSourceId) {
        super(orgId, tenantId, connectionId);
        this.dataSourceId = dataSourceId;
    }
}
