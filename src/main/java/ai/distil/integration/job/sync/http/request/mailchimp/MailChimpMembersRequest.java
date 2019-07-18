package ai.distil.integration.job.sync.http.request.mailchimp;

import ai.distil.integration.controller.dto.data.DatasetPageRequest;
import ai.distil.integration.job.sync.http.mailchimp.vo.MembersWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.asynchttpclient.Param;
import org.springframework.http.HttpMethod;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class MailChimpMembersRequest extends AbstractMailChimpRequest<MembersWrapper> {
    private static final String url = "/lists/%s/members";

    private String listId;
    private DatasetPageRequest pageRequest;

    public MailChimpMembersRequest(String listId, String apiKey, DatasetPageRequest pageRequest) {
        super(apiKey);
        this.listId = listId;
        this.pageRequest = pageRequest;
    }

    @Override
    public TypeReference<MembersWrapper> resultType() {
        return new TypeReference<MembersWrapper>() {};
    }

    @Override
    public String urlPart() {
        return String.format(url, listId);
    }

    @Override
    public HttpMethod httpMethod() {
        return HttpMethod.GET;
    }

    @Override
    public List<Param> params() {
        return buildDefaultPageParams(pageRequest);
    }
}
