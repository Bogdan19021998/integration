package ai.distil.integration.job.sync.http.campmon.request.ingestion;

import ai.distil.integration.job.sync.http.campmon.request.AbstractPostCampaignMonitorRequest;
import ai.distil.integration.job.sync.http.campmon.request.ingestion.vo.CreateListBody;
import com.fasterxml.jackson.core.type.TypeReference;

public class CreateListCampaignMonitorRequest extends AbstractPostCampaignMonitorRequest<String, CreateListBody> {

    private String clientId;

    public CreateListCampaignMonitorRequest(String clientId, String apiKey, CreateListBody body) {
        super(apiKey, body);
        this.clientId = clientId;
    }

    @Override
    public TypeReference<String> resultType() {
        return new TypeReference<String>(){};
    }

    @Override
    public String urlPart() {
        return String.format("/lists/%s.json", this.clientId);
    }
}
