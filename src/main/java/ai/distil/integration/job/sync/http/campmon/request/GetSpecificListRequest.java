package ai.distil.integration.job.sync.http.campmon.request;

import ai.distil.integration.job.sync.http.campmon.vo.SpecificList;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.http.HttpMethod;

public class GetSpecificListRequest extends AbstractCampaignMonitorRequest<SpecificList> {

    private String listId;

    public GetSpecificListRequest(String apiKey, String listId) {
        super(apiKey);
        this.listId = listId;
    }

    @Override
    public TypeReference<SpecificList> resultType() {
        return new TypeReference<SpecificList>() {};
    }

    @Override
    public String urlPart() {
        return String.format("/lists/%s.json", listId);
    }

    @Override
    public HttpMethod httpMethod() {
        return HttpMethod.GET;
    }
}
