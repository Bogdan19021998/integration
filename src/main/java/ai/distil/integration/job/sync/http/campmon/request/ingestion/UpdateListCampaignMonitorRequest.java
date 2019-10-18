package ai.distil.integration.job.sync.http.campmon.request.ingestion;

import ai.distil.integration.job.sync.http.campmon.request.AbstractPostCampaignMonitorRequest;
import ai.distil.integration.job.sync.http.campmon.request.ingestion.vo.ListBody;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.http.HttpMethod;

public class UpdateListCampaignMonitorRequest extends AbstractPostCampaignMonitorRequest<String, ListBody> {

    private String listId;

    public UpdateListCampaignMonitorRequest(String listId, String apiKey, ListBody body) {
        super(apiKey, body);
        this.listId = listId;
    }

    @Override
    public TypeReference<String> resultType() {
        return new TypeReference<String>(){};
    }

    @Override
    public String urlPart() {
        return String.format("/lists/%s.json", this.listId);
    }


    @Override
    public HttpMethod httpMethod() {
        return HttpMethod.PUT;
    }
}
