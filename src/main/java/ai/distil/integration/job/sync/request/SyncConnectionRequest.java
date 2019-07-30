package ai.distil.integration.job.sync.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class SyncConnectionRequest implements IJobRequest, Serializable {

    private static final long serialVersionUID = -5019757636334640002L;

    private Long orgId;
    private String tenantId;
    private Long connectionId;

    @Override
    public String getKey() {
        return tenantId +
                DEFAULT_KEY_SEPARATOR +
                connectionId;
    }
}
