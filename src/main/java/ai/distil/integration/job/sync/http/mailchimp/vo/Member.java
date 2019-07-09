package ai.distil.integration.job.sync.http.mailchimp.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
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
public class Member {
    private String id;
    private String emailAddress;
    private String uniqueEmailId;
    private Long webId;
    private String emailType;
    private String status;
    private String unsubscribeReason;
    private Map<String, Object> mergeFields;
    private Map<String, Object> interests;
    private MemberStats stats;

    private String ipSignup;
    private String timestampSignup;
    private String ipOpt;
    private String timestampOpt;
    private Long memberRating;
    private String lastChanged;
    private String language;
    private Boolean vip;
    private String emailClient;
    private Location location;
    private List<MarketingPermissions> marketingPermissions;

    private Note lastNote;

    private String source;
    private Long tagsCount;
    private List<String> tags;

    private String listId;

    @JsonProperty("_links")
    private List<Link> links;


}
