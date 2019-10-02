package ai.distil.integration.job.sync.http.campmon.request.ingestion.vo;

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
public class SubscribersImportResponse {
    private List<Object> failureDetails;
    private Integer totalUniqueEmailsSubmitted;
    private Integer totalExistingSubscribers;
    private Integer totalNewSubscribers;

    private List<Object> duplicateEmailsInSubmission;
}
