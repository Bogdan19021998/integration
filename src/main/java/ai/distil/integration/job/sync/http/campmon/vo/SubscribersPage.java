package ai.distil.integration.job.sync.http.campmon.vo;

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
@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
public class SubscribersPage {
    private List<Map<String, Object>> results;
    private String resultsOrderedBy;
    private String orderDirection;
    private Integer pageNumber;
    private Integer pageSize;
    private Integer recordsOnThisPage;
    private Integer totalNumberOfRecords;
    private Integer numberOfPages;
}
