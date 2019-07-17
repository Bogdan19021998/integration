package ai.distil.integration.job.sync.http.request;

import com.fasterxml.jackson.core.type.TypeReference;
import com.mysql.jdbc.StringUtils;
import org.asynchttpclient.Param;
import org.springframework.http.HttpMethod;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public interface IHttpRequest<R> {
    String AUTH_HEADER_KEY = "Authorization";

    TypeReference<R> resultType();

    String urlPart();

    HttpMethod httpMethod();

    default Map<String, Object> headers() {
        return Collections.emptyMap();
    }

    default List<Param> params() {
        return Collections.emptyList();
    }

    default Object body() {
        return null;
    }

    default String buildBasicAuthHeader(String token) {
        return String.format("Basic %s", token);
    }

    default String buildRequestParams(Map<String, String> map) {
        String requestParams = map.entrySet()
                .stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining("&"));

        return StringUtils.isNullOrEmpty(requestParams) ? "" : ("?" + requestParams);
    }

}
