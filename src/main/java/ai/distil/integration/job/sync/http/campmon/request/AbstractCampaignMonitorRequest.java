package ai.distil.integration.job.sync.http.campmon.request;

import ai.distil.integration.job.sync.http.request.IHttpRequest;
import lombok.AllArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
public abstract class AbstractCampaignMonitorRequest<T> implements IHttpRequest<T> {
    private String apiKey;

    @Override
    public Map<String, Object> headers() {
        return new HashMap<String, Object>(){{
            this.put(AUTH_HEADER_KEY, buildBasicAuthHeader(apiKey));
        }};
    }
}
