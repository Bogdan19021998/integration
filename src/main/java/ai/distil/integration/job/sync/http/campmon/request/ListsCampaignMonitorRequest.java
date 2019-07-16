package ai.distil.integration.job.sync.http.campmon.request;

import ai.distil.integration.job.sync.http.campmon.vo.Link;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.http.HttpMethod;

import java.util.List;

public class ListsCampaignMonitorRequest extends AbstractCampaignMonitorRequest<List<Link>> {

    private String clientId;

    public ListsCampaignMonitorRequest(String apiKey, String clientId) {
        super(apiKey);
        this.clientId = clientId;
    }

    @Override
    public TypeReference<List<Link>> resultType() {
        return new TypeReference<List<Link>>() {};
    }

    @Override
    public String urlPart() {
        return String.format("/clients/%s/lists.json", this.clientId);
    }

    @Override
    public HttpMethod httpMethod() {
        return HttpMethod.GET;
    }
}
