package ai.distil.integration.job.sync.http.request.mailchimp.ingestion;

import ai.distil.integration.job.sync.http.mailchimp.vo.Audience;
import ai.distil.integration.job.sync.http.mailchimp.vo.MailChimpList;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.http.HttpMethod;

public class EditListMailChimpRequest extends AbstractPostMailChimpRequest<Audience, MailChimpList> {

    private String listId;

    public EditListMailChimpRequest(String apiKey, MailChimpList body, String listId) {
        super(apiKey, body);
        this.listId = listId;
    }

    @Override
    public TypeReference<Audience> resultType() {
        return new TypeReference<Audience>() {
        };
    }

    @Override
    public String urlPart() {
        return String.format("/lists/%s", listId);
    }

    @Override
    public HttpMethod httpMethod() {
        return HttpMethod.PATCH;
    }
}
