package ai.distil.integration.job.sync.http.campmon.vo;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
public class CustomFieldDefinition {
    private String fieldName;
    private String key;
    private String dataType;
    private List<String> fieldOptions;
    private Boolean visibleInPreferenceCenter;
}
