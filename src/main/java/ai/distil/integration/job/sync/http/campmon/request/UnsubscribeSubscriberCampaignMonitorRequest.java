package ai.distil.integration.job.sync.http.campmon.request;

import ai.distil.integration.job.sync.http.campmon.vo.UnsubscribeRequest;
import ai.distil.integration.job.sync.http.request.mailchimp.ingestion.AbstractPostMailChimpRequest;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@EqualsAndHashCode(callSuper = true)
public class UnsubscribeSubscriberCampaignMonitorRequest extends AbstractPostMailChimpRequest<String, UnsubscribeRequest> {
    private String listId;

    public UnsubscribeSubscriberCampaignMonitorRequest(String apiKey, String listId, UnsubscribeRequest request) {
        super(apiKey, request);
        this.listId = listId;
    }

    @Override
    public TypeReference<String> resultType() {
        return new TypeReference<String>() {};
    }

    @Override
    public String urlPart() {
        return String.format("/subscribers/%s/unsubscribe.json", listId);
    }

}
