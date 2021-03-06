package ai.distil.integration.job.sync.http.campmon.request;

import ai.distil.integration.controller.dto.data.DatasetPageRequest;
import ai.distil.integration.job.sync.http.campmon.vo.SubscribersPage;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.http.HttpMethod;


public class SubscribersCampaignMonitorRequest extends AbstractCampaignMonitorRequest<SubscribersPage> {
    private String listId;
    private DatasetPageRequest pageRequest;

    public SubscribersCampaignMonitorRequest(String apiKey, String listId, DatasetPageRequest pageRequest) {
        super(apiKey);
        this.listId = listId;
        this.pageRequest = pageRequest;
    }

    @Override
    public TypeReference<SubscribersPage> resultType() {
        return new TypeReference<SubscribersPage>() {
        };
    }

    @Override
    public String urlPart() {
        return String.format("/lists/%s/active.json?page=%s&pagesize=%s&includetrackingpreference=true",
                listId,
                pageRequest.getPageNumber(),
                pageRequest.getPageSize());
    }

    @Override
    public HttpMethod httpMethod() {
        return HttpMethod.GET;
    }
}
