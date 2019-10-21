package ai.distil.integration.job.sync.http.campmon.request.ingestion;

import ai.distil.integration.job.sync.http.campmon.request.AbstractPostCampaignMonitorRequest;
import ai.distil.integration.job.sync.http.campmon.request.ingestion.vo.CreateCustomFieldBody;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CreateCustomFieldCampaignMonitorRequest extends AbstractPostCampaignMonitorRequest<String, CreateCustomFieldBody> {

    private String listId;

    public CreateCustomFieldCampaignMonitorRequest(String listId, String apiKey, CreateCustomFieldBody body) {
        super(apiKey, body);
        this.listId = listId;
    }

    @Override
    public TypeReference<String> resultType() {
        return new TypeReference<String>(){};
    }

    @Override
    public String urlPart() {
        return String.format("/lists/%s/customfields.json", this.listId);
    }
}
