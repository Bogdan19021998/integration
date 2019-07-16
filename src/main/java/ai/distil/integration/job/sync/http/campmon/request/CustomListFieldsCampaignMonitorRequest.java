package ai.distil.integration.job.sync.http.campmon.request;

import ai.distil.integration.job.sync.http.campmon.vo.CustomFieldDefinition;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.http.HttpMethod;

import java.util.List;

public class CustomListFieldsCampaignMonitorRequest extends AbstractCampaignMonitorRequest<List<CustomFieldDefinition>> {
    private String listId;

    public CustomListFieldsCampaignMonitorRequest(String apiKey, String listId) {
        super(apiKey);
        this.listId = listId;
    }

    @Override
    public TypeReference<List<CustomFieldDefinition>> resultType() {
        return new TypeReference<List<CustomFieldDefinition>>() {};
    }

    @Override
    public String urlPart() {
        return String.format("/lists/%s/customfields.json", listId);
    }

    @Override
    public HttpMethod httpMethod() {
        return HttpMethod.GET;
    }
}
