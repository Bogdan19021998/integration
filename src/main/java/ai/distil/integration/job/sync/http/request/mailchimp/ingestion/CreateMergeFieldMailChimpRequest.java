package ai.distil.integration.job.sync.http.request.mailchimp.ingestion;

import ai.distil.integration.job.sync.http.mailchimp.vo.MailChimpMergeField;
import ai.distil.integration.job.sync.http.mailchimp.vo.MailChimpMergeFieldResponse;
import com.fasterxml.jackson.core.type.TypeReference;

public class CreateMergeFieldMailChimpRequest extends AbstractPostMailChimpRequest<MailChimpMergeFieldResponse, MailChimpMergeField> {

    private String listId;

    public CreateMergeFieldMailChimpRequest(String apiKey, String listId, MailChimpMergeField body) {
        super(apiKey, body);
        this.listId = listId;
    }

    @Override
    public TypeReference<MailChimpMergeFieldResponse> resultType() {
        return new TypeReference<MailChimpMergeFieldResponse>() {};
    }

    @Override
    public String urlPart() {
        return String.format("/lists/%s/merge-fields", this.listId);
    }
}
