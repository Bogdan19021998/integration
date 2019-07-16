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
public class Subscriber {
    private String emailAddress;
    private String name;
    private String date;
    private String state;
    private List<CustomField> customFields;
    private String readsEmailWith;


}
