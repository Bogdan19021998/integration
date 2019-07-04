package ai.distil.integration.job.sync.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class SyncDataSourceRequest implements IJobRequest, Serializable {

    private static final long serialVersionUID = -5019757636334640002L;

    private Long orgId;
    private Long connectionId;
    private Long dataSourceId;

    @Override
    public String getKey() {
        return new StringBuilder()
                .append(orgId)
                .append(DEFAULT_KEY_SEPARATOR)
                .append(connectionId)
                .append(DEFAULT_KEY_SEPARATOR)
                .append(dataSourceId)
                .toString();
    }
}
