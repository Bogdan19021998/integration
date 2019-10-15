package ai.distil.integration.job.sync.http.campmon.request;

import ai.distil.integration.job.sync.http.IDataConverter;
import ai.distil.integration.job.sync.http.campmon.request.ingestion.vo.CampaignMonitorPostError;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.asynchttpclient.Response;
import org.springframework.http.HttpMethod;

@Slf4j
public abstract class AbstractPostCampaignMonitorRequest<T, B> extends AbstractCampaignMonitorRequest<T> {

    @Getter
    private B body;

    public AbstractPostCampaignMonitorRequest(String apiKey, B body) {
        super(apiKey);
        this.body = body;
    }

    @Override
    public void handleError(Response response, IDataConverter converter) {
        CampaignMonitorPostError error;
        try {
            error = converter.fromString(response.getResponseBody(), CampaignMonitorPostError.class);
        } catch (Exception e) {
            log.error("Unable to handle error {}", response.getResponseBody(), e);
            return;
        }

        switch (error.getCode()) {
//                this means that thing already created, and it's fine
            case "250":
            case "255":
                break;
//                failed to ingest some customers, just log it
            case "210":
                log.warn("Can't ingest customers data - failure details: {}", response.getResponseBody());
                return;
            default:
                throw new RuntimeException(String.format("Unexpected error happen, can't proceed with it. Details - %s", error));
        }

    }

    @Override
    public HttpMethod httpMethod() {
        return HttpMethod.POST;
    }
}
