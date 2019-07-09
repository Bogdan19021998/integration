package ai.distil.integration.job.sync.http.mailchimp.vo;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class CampaignDefaults {
    private String fromName;
    private String fromEmail;
    private String subject;
    private String language;
}
