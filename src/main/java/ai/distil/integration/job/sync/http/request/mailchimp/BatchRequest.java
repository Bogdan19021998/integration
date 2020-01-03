package ai.distil.integration.job.sync.http.request.mailchimp;

import ai.distil.integration.job.sync.http.IDataConverter;
import ai.distil.integration.job.sync.http.mailchimp.vo.batch.BatchRequestBody;
import ai.distil.integration.job.sync.http.mailchimp.vo.batch.BatchTrackingResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.Getter;
import org.springframework.http.HttpMethod;

import java.util.List;

public class BatchRequest extends AbstractMailChimpRequest<BatchTrackingResponse> {

    @Getter
    private BatchRequestBody body;

    public BatchRequest(String apiKey, List<? extends AbstractMailChimpRequest> requests, IDataConverter converter) {
        super(apiKey);
        this.body = new BatchRequestBody(requests, converter);
    }


    @Override
    public TypeReference<BatchTrackingResponse> resultType() {
        return new TypeReference<BatchTrackingResponse>() {
        };
    }

    @Override
    public String urlPart() {
        return "/batches";
    }

    @Override
    public HttpMethod httpMethod() {
        return HttpMethod.POST;
    }
}
