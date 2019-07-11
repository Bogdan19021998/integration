package ai.distil.integration.job.sync.http.request.mailchimp;

import ai.distil.integration.job.sync.http.mailchimp.vo.Audience;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpMethod;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SingleMailChimpAudienceRequest implements IMailChimpRequest<Audience> {
    private String listId;

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
