package ai.distil.integration.job.sync.http.mailchimp.vo.batch;


import ai.distil.integration.job.sync.http.mailchimp.vo.Link;
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
public class BatchTrackingResponse {
    private String id;
    private String status;
    private Integer totalOperations;
    private Integer finishedOperations;

    private String responseBodyUrl;

    private Boolean erroredOperations;
    private String submittedAt;
    private String completedAt;

    @JsonProperty("_links")
    private List<Link> links;

}
