package ai.distil.integration.job.sync.http.campmon.request.ingestion.vo;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
public class CreateCustomFieldBody {

    private String fieldName;
    private String dataType;
    private Boolean visibleInPreferenceCenter;

}
