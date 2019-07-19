package ai.distil.integration.job.sync.http.sf.request;

import ai.distil.integration.job.sync.http.request.IHttpRequest;
import lombok.AllArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
public abstract class AbstractSalesforceRequest<T> implements IHttpRequest<T> {
    protected String accessToken;
    protected String apiVersion;

    @Override
    public Map<String, Object> headers() {
        Map<String, Object> headers = new HashMap<>();
        headers.put(AUTH_HEADER_KEY, bearerAuthKey(accessToken));
        return headers;
    }
}
