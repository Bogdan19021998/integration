package ai.distil.integration.job.sync.jdbc;

import ai.distil.integration.job.sync.jdbc.vo.TableType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SimpleDataSourceDefinition {
    private String dbName;
    private String dataSourceId;
    private TableType tableType;
    private String description;
}
