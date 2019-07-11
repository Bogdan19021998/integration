package ai.distil.integration.job.sync.http.request.mailchimp;

import ai.distil.integration.job.sync.http.mailchimp.vo.Audience;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.http.HttpMethod;

@Data
@EqualsAndHashCode(callSuper = true)
public class SingleMailChimpAudienceRequest extends AbstractMailChimpRequest<Audience> {
    private String listId;

    public SingleMailChimpAudienceRequest(String apiKey, String listId) {
        super(apiKey);
        this.listId = listId;
    }

    @Override
    public TypeReference<Audience> resultType() {
        return new TypeReference<Audience>(){};
    }

    @Override
    public String urlPart() {
        return String.format("/lists/%s", listId);
    }

    @Override
    public HttpMethod httpMethod() {
        return HttpMethod.GET;
    }
}
