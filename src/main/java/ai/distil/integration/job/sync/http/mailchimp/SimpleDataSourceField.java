package ai.distil.integration.job.sync.http.mailchimp;

import ai.distil.model.types.CassandraDataSourceAttributeType;
import ai.distil.model.types.DataSourceSchemaAttributeTag;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SimpleDataSourceField {
    private String sourceFieldName;
    private String displayName;
    private CassandraDataSourceAttributeType attributeType;
    private DataSourceSchemaAttributeTag attributeTag;

}
