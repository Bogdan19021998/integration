package ai.distil.integration.job.sync.jdbc.vo;

import ai.distil.model.types.CassandraDataSourceAttributeType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ColumnDefinition {

    private Integer position;
    private String columnName;
    private CassandraDataSourceAttributeType dataType;
}
