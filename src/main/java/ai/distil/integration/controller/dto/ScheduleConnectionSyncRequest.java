package ai.distil.integration.controller.dto;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ScheduleConnectionSyncRequest extends CommonConnectionRequest {
    public ScheduleConnectionSyncRequest(Long orgId, String tenantId, Long connectionId) {
        super(orgId, tenantId, connectionId);
    }
}
