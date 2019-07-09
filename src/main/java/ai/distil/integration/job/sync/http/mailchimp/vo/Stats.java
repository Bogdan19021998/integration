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
public class Stats {
    private Long memberCount;
    private Long totalContacts;
    private Long unsubscribeCount;
    private Long cleanedCount;
    private Long memberCountSinceSend;
    private Long unsubscribeCountSinceSend;
    private Long cleanedCountSinceSend;
    private Long campaignCount;
    private String campaignLastSent;
    private Long mergeFieldCount;
    private Double avgSubRate;
    private Double avgUnsubRate;
    private Double openRate;
    private Double clickRate;
    private String lastSubDate;
    private String lastUnsubDate;
}
