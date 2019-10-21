package ai.distil.integration.job.sync.http.request.mailchimp.ingestion;

import ai.distil.integration.job.sync.http.request.mailchimp.AbstractMailChimpRequest;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.Getter;
import org.springframework.http.HttpMethod;

public class DeleteMemberMailChimpRequest extends AbstractMailChimpRequest<String> {

    @Getter
    private String listId;
    @Getter
    private String hash;


    public DeleteMemberMailChimpRequest(String apiKey, String listId, String hash) {
        super(apiKey);
        this.listId = listId;
        this.hash = hash;
    }

    @Override
    public TypeReference<String> resultType() {
        return new TypeReference<String>() {
        };
    }

    @Override
    public String urlPart() {
        return String.format("/lists/%s/members/%s", this.listId, this.hash);
    }

    @Override
    public HttpMethod httpMethod() {
        return HttpMethod.DELETE;
    }
}
