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
public class ResponseWrapper<T> {

    @JsonProperty("lists")
    private List<T> list;

    private Long totalItems;
    private Constraints constraints;

    @JsonProperty("_links")
    private List<Link> links;
}
