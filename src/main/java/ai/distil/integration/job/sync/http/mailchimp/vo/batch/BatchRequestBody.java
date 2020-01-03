package ai.distil.integration.job.sync.http.mailchimp.vo.batch;

import ai.distil.integration.job.sync.http.IDataConverter;
import ai.distil.integration.job.sync.http.request.mailchimp.AbstractMailChimpRequest;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.asynchttpclient.Param;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;


@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class BatchRequestBody {


    private List<Operation> operations;


    public BatchRequestBody(List<? extends AbstractMailChimpRequest> requests, IDataConverter converter) {
        this.operations = requests.stream().map(v -> new Operation(v, converter)).collect(Collectors.toList());
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
    public static class Operation<R, T extends AbstractMailChimpRequest<R>> {
        //The HTTP method to use for the operation.
        private String method;
        //The relative path to use for the operation.
        private String path;
        //Any URL params, only used for GET
        private Map<String, String> params;
        //The JSON payload for PUT, POST, or PATCH
        private Object body;

        private String operationId;

        public Operation(T t, IDataConverter converter) {
            this.method = t.httpMethod().toString();
            this.path = t.urlPart();
            this.params = t.params().stream().collect(Collectors.toMap(Param::getName, Param::getValue));
            this.body = converter.toString(t.getBody());
            this.operationId = UUID.randomUUID().toString();
        }
    }
}
