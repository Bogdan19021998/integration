package ai.distil.integration.job.sync.http.request.mailchimp.ingestion;

import ai.distil.integration.job.sync.http.mailchimp.vo.Audience;
import ai.distil.integration.job.sync.http.mailchimp.vo.MailChimpList;
import com.fasterxml.jackson.core.type.TypeReference;

public class CreateListMailChimpRequest extends AbstractPostMailChimpRequest<Audience, MailChimpList> {

    public CreateListMailChimpRequest(String apiKey, MailChimpList body) {
        super(apiKey, body);
    }

    @Override
    public TypeReference<Audience> resultType() {
        return new TypeReference<Audience>() {
        };
    }

    @Override
    public String urlPart() {
        return "/lists";
    }
}
