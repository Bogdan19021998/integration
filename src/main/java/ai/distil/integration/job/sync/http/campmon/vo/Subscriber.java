package ai.distil.integration.job.sync.http.campmon.vo;

import ai.distil.integration.job.sync.AbstractSubscriber;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

import static ai.distil.integration.job.sync.http.campmon.holder.CampaignMonitorFieldsHolder.formatCustomFieldName;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
public class Subscriber extends AbstractSubscriber {
    public static final String DISTIL_HASH_FIELD = formatCustomFieldName(HASH_CODE_FRIENDLY_NAME.replaceAll(" ", ""));

    private String emailAddress;
    private String name;
    private List<CustomField> customFields;
    private Boolean resubscribe = true;
    private Boolean restartSubscriptionBasedAutoresponders = true;
    private String consentToTrack = "Yes";

    @JsonIgnore
    private String hashCode;


    @Override
    public void setHashCode(String field, String hashCode) {
        this.hashCode = hashCode;
        this.customFields.add(new CustomField(DISTIL_HASH_FIELD, hashCode));
    }

    @Override
    public void setEmail(String value) {
        this.emailAddress = value;
    }

    @Override
    public void setFirstName(String value) {
//        todo?
    }

    @Override
    public void setLastName(String value) {
//        todo?
    }
}
