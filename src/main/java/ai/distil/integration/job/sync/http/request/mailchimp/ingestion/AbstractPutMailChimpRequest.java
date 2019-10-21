package ai.distil.integration.job.sync.http.request.mailchimp.ingestion;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;

@Slf4j
public abstract class AbstractPutMailChimpRequest<T, B> extends AbstractPostMailChimpRequest<T, B> {

    public AbstractPutMailChimpRequest(String apiKey, B body) {
        super(apiKey, body);
    }

    @Override
    public HttpMethod httpMethod() {
        return HttpMethod.PUT;
    }
}
