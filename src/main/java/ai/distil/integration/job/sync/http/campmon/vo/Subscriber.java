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

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
public class Subscriber extends AbstractSubscriber {
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
        this.customFields.add(new CustomField(field, hashCode));
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
