package ai.distil.integration.job.sync.http.request.mailchimp;

import ai.distil.integration.job.sync.http.mailchimp.vo.AudiencesWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.http.HttpMethod;

public class MailChimpAudiencesRequest extends AbstractMailChimpRequest<AudiencesWrapper> {
    public MailChimpAudiencesRequest(String apiKey) {
        super(apiKey);
    }

    @Override
    public TypeReference<AudiencesWrapper> resultType() {
        return new TypeReference<AudiencesWrapper>() {};
    }

    @Override
    public String urlPart() {
        return "/lists";
    }

    @Override
    public HttpMethod httpMethod() {
        return HttpMethod.GET;
    }

}
