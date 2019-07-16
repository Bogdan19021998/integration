package ai.distil.integration.job.sync.http.campmon.request;

import ai.distil.integration.job.sync.http.campmon.vo.Client;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.http.HttpMethod;

import java.util.List;

public class ClientsCampaignMonitorRequest extends AbstractCampaignMonitorRequest<List<Client>> {
    public ClientsCampaignMonitorRequest(String apiKey) {
        super(apiKey);
    }

    @Override
    public TypeReference<List<Client>> resultType() {
        return new TypeReference<List<Client>>() {};
    }

    @Override
    public String urlPart() {
        return "/clients.json";
    }

    @Override
    public HttpMethod httpMethod() {
        return HttpMethod.GET;
    }
}
