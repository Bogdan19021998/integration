package ai.distil.integration.job.sync.http.campmon.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
public class SpecificList {
    @JsonProperty("ListID")
    private String listId;
    private String title;
    private String unsubscribePage;
    private String confirmedOptIn;
    private String confirmationSuccessPage;
    private String unsubscribeSetting;
}
