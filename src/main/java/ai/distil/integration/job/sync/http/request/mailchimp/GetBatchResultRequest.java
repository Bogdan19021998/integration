package ai.distil.integration.job.sync.http.request.mailchimp;

import ai.distil.integration.job.sync.http.mailchimp.vo.batch.BatchOperationResult;
import ai.distil.integration.job.sync.http.request.IHttpRequest;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.http.HttpMethod;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GetBatchResultRequest<T> implements IHttpRequest<List<BatchOperationResult<T>>> {

    @Override
    public TypeReference<List<BatchOperationResult<T>>> resultType() {
        return new TypeReference<List<BatchOperationResult<T>>>() {
        };
    }

    @Override
    public String urlPart() {
        return "";
    }

    @Override
    public HttpMethod httpMethod() {
        return HttpMethod.GET;
    }

    @Override
    public Map<String, Object> headers() {
        Map<String, Object> map = new HashMap<>();
        return map;
    }

    @Override
    public boolean fromBytes() {
        return true;
    }
}
