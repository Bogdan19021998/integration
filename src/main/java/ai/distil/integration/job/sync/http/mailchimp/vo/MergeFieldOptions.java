package ai.distil.integration.job.sync.http.mailchimp.vo;

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
public class MergeFieldOptions {
    private Long defaultCountry;
    private String phoneFormat;
    private String dateFormat;
    private List<Object> choices;
    private Integer size;
}
