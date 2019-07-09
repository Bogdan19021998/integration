package ai.distil.integration.job.sync.http.mailchimp.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class Audience {
    private String id;
    private Integer webId;
    private String name;
    private Contact contact;
    private String permissionReminder;
    private Boolean useArchiveBar;

    private CampaignDefaults campaignDefaults;

    private String notifyOnSubscribe;
    private String notifyOnUnsubscribe;
    private String dateCreated;
    private Long listRating;
    private Boolean emailTypeOption;
    private String subscribeUrlShort;
    private String subscribeUrlLong;
    private String beamerAddress;
    private String visibility;
    private Boolean doubleOptin;
    private Boolean hasWelcome;
    private Boolean marketingPermissions;
    private List<String> modules;

    private Stats stats;

    @JsonProperty(value = "_links")
    private List<Link> links;

}
