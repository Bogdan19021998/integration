package ai.distil.integration.job.sync.http.mailchimp.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
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
@EqualsAndHashCode(callSuper = true)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class Member extends InsertMember {

    private String id;
    private String uniqueEmailId;
    private Long webId;
    private String unsubscribeReason;
    private MemberStats stats;
    private Long memberRating;
    private String lastChanged;
    private String emailClient;
    private Note lastNote;
    private String source;
    private Long tagsCount;
    private List<String> tags;

    private String listId;

    @JsonProperty("_links")
    private List<Link> links;


}
