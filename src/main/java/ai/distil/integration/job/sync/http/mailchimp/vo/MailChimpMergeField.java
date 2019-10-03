package ai.distil.integration.job.sync.http.mailchimp.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class MailChimpMergeField {
    private String tag;
    private String name;
    private String type;
    private Boolean required;
    private String defaultValue;
    @JsonProperty("public")
    private Boolean publicField;
    private Integer displayOrder;
    private MergeFieldOptions options;
    private String helpText;
}
