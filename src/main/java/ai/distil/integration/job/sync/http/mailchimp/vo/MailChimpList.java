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
public class MailChimpList {
    private String name;
    private Contact contact;
    private String permissionReminder;
    private Boolean useArchiveBar;

    private CampaignDefaults campaignDefaults;

    private String notifyOnSubscribe;
    private String notifyOnUnsubscribe;
    private Boolean emailTypeOption;

    private String visibility;
    private Boolean doubleOptin;
    private Boolean marketingPermissions;

}
