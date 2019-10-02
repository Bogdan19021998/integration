package ai.distil.integration.job.sync.http.campmon.request.ingestion;

import ai.distil.integration.job.sync.http.campmon.request.AbstractPostCampaignMonitorRequest;
import ai.distil.integration.job.sync.http.campmon.request.ingestion.vo.SubscribersImportResponse;
import ai.distil.integration.job.sync.http.campmon.vo.Subscribers;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ImportSubscribersCampaignMonitorRequest extends AbstractPostCampaignMonitorRequest<SubscribersImportResponse, Subscribers> {

    private String listId;

    public ImportSubscribersCampaignMonitorRequest(String apiKey, String listId, Subscribers body) {
        super(apiKey, body);
        this.listId = listId;
    }

    @Override
    public TypeReference<SubscribersImportResponse> resultType() {
        return new TypeReference<SubscribersImportResponse>() {};
    }

    @Override
    public String urlPart() {
        return String.format("/subscribers/%s/import.json", this.listId);
    }
}
