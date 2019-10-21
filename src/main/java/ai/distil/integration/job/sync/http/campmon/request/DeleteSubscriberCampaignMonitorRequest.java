package ai.distil.integration.job.sync.http.campmon.request;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import org.asynchttpclient.Param;
import org.springframework.http.HttpMethod;

import java.util.List;


public class DeleteSubscriberCampaignMonitorRequest extends AbstractCampaignMonitorRequest<String> {
    public static final String EMAIL_PARAM = "email";
    private String listId;
    private String email;

    public DeleteSubscriberCampaignMonitorRequest(String apiKey, String listId, String email) {
        super(apiKey);
        this.email = email;
        this.listId = listId;
    }

    @Override
    public TypeReference<String> resultType() {
        return new TypeReference<String>() {
        };
    }

    @Override
    public String urlPart() {
        return String.format("/subscribers/%s.json", listId);
    }

    @Override
    public HttpMethod httpMethod() {
        return HttpMethod.DELETE;
    }

    @Override
    public List<Param> params() {
        return Lists.newArrayList(
                new Param(EMAIL_PARAM, this.email)
        );
    }
}
