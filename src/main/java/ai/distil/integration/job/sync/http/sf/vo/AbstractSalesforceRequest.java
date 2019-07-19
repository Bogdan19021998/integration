package ai.distil.integration.job.sync.http.sf.vo;


import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public abstract class AbstractSalesforceRequest {
}
