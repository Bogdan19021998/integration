package ai.distil.integration.job.sync.http.mailchimp.vo;

import ai.distil.integration.job.sync.AbstractSubscriber;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class InsertMember extends AbstractSubscriber {

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

    @JsonIgnore
    private String hashCode;

    @Override
    public void setHashCode(String fieldId, String hashCode) {
        this.hashCode = hashCode;
        this.mergeFields.put(fieldId, hashCode);
    }

    @Override
    public void setEmail(String value) {
        this.emailAddress = value;
    }

    @Override
    public void setFirstName(String value) {
        this.mergeFields.put(FIRST_NAME_FIELD, value);
    }

    @Override
    public void setLastName(String value) {
        this.mergeFields.put(LAST_NAME_FIELD, value);
    }

    private static final String FIRST_NAME_FIELD = "FNAME";
    private static final String LAST_NAME_FIELD = "LNAME";

}
