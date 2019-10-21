package ai.distil.integration.job.sync.http.request.mailchimp.ingestion;

import ai.distil.integration.job.sync.http.IDataConverter;
import ai.distil.integration.job.sync.http.mailchimp.vo.error.MailChimpResponseError;
import ai.distil.integration.job.sync.http.request.mailchimp.AbstractMailChimpRequest;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.asynchttpclient.Response;
import org.springframework.http.HttpMethod;

@Slf4j
public abstract class AbstractPostMailChimpRequest<T, B> extends AbstractMailChimpRequest<T> {
    @Getter
    private B body;

    public AbstractPostMailChimpRequest(String apiKey, B body) {
        super(apiKey);
        this.body = body;
    }

    @Override
    public void handleError(Response response, IDataConverter converter) {
        try {
            MailChimpResponseError error = converter.fromString(response.getResponseBody(), MailChimpResponseError.class);
            log.error("Error details: {}", error);
        } catch (Exception e) {
            log.error("Can't deserialize mailchimp response: {}", response.getResponseBody(), e);
        }
    }

    @Override
    public HttpMethod httpMethod() {
        return HttpMethod.POST;
    }
}
