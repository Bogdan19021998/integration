package ai.distil.integration.job.sync.http.mailchimp.vo;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class InsertMember {

    private String emailAddress;
    private String statusIfNew;
    private String emailType;
    private String status;
    private Map<String, Object> mergeFields;
    private Map<String, Object> interests;
    private String language;
    private Boolean vip;
    private Location location;
    private List<MarketingPermissions> marketingPermissions;
    private String ipSignup;
    private String timestampSignup;
    private String ipOpt;
    private String timestampOpt;

}
