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
public class ListBody {

    private String title;
    private String unsubscribeSetting;
    private Boolean confirmedOptIn;

}
