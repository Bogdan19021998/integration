package ai.distil.integration.job.sync.http.request.mailchimp;

import ai.distil.integration.job.sync.http.mailchimp.vo.MembersWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.asynchttpclient.Param;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpMethod;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MailChimpMembersRequest implements IMailChimpRequest<MembersWrapper> {
    private static final String url = "/lists/%s/members";

    private String listId;
    private PageRequest pageRequest;

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
