package ai.distil.integration.job.destination.vo;

import ai.distil.model.types.DataSourceSchemaAttributeTag;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CustomAttributeDefinition {
    private String id;
    private String name;
    private DataSourceSchemaAttributeTag tag;
    private Long distilAttributeId;
}
