package ai.distil.integration.job.sync.http.request.mailchimp;

import ai.distil.integration.job.sync.http.mailchimp.vo.batch.BatchTrackingResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.http.HttpMethod;

public class GetBatchDataRequest extends AbstractMailChimpRequest<BatchTrackingResponse> {

    private String batchId;

    public GetBatchDataRequest(String apiKey, String batchId) {
        super(apiKey);
        this.batchId = batchId;
    }

    @Override
    public TypeReference<BatchTrackingResponse> resultType() {
        return new TypeReference<BatchTrackingResponse>() {
        };
    }

    @Override
    public String urlPart() {
        return "/batches/" + this.batchId;
    }

    @Override
    public HttpMethod httpMethod() {
        return HttpMethod.GET;
    }

}
