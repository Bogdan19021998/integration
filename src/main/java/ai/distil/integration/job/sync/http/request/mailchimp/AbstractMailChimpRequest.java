package ai.distil.integration.job.sync.http.request.mailchimp;

import ai.distil.integration.job.sync.http.request.IHttpRequest;
import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import org.asynchttpclient.Param;
import org.springframework.data.domain.PageRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
public abstract class AbstractMailChimpRequest<R> implements IHttpRequest<R> {
    protected static final String DEFAULT_COUNT_KEY = "count";
    protected static final String DEFAULT_OFFSET_KEY = "offset";

    private String apiKey;

    protected List<Param> buildDefaultPageParams(PageRequest pageRequest) {
        return Lists.newArrayList(
                new Param(DEFAULT_COUNT_KEY, String.valueOf(pageRequest.getPageSize())),
                new Param(DEFAULT_OFFSET_KEY, String.valueOf(pageRequest.getPageNumber() * pageRequest.getPageSize()))
        );
    }

    @Override
    public Map<String, Object> headers() {
        Map<String, Object> headers = new HashMap<>();
        headers.put(AUTH_HEADER_KEY, buildBasicAuthHeader(apiKey));
        return headers;
    }
}
