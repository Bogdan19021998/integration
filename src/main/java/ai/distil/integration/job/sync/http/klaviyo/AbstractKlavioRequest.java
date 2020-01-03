package ai.distil.integration.job.sync.http.klaviyo;

import ai.distil.integration.job.sync.http.request.IHttpRequest;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.http.HttpMethod;

public class AbstractKlavioRequest<R> implements IHttpRequest<R> {

    @Override
    public TypeReference<R> resultType() {
        return null;
    }

    @Override
    public String urlPart() {
        return null;
    }

    @Override
    public HttpMethod httpMethod() {
        return null;
    }
}
