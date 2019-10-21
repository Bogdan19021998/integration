package ai.distil.integration.job.sync.http.request.mailchimp;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.asynchttpclient.Param;
import org.springframework.http.HttpMethod;

import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
public class MailChimpMergeFieldsRequest extends AbstractMailChimpRequest<Map<String, Object>> {

    private static final String MERGE_FIELDS_COUNT = "1000";

    private String listId;

    public MailChimpMergeFieldsRequest(String apiKey, String listId) {
        super(apiKey);
        this.listId = listId;
    }

    @Override
    public TypeReference<Map<String, Object>> resultType() {
        return new TypeReference<Map<String, Object>>() {};
    }

    @Override
    public String urlPart() {
        return String.format("/lists/%s/merge-fields", listId);
    }

    @Override
    public HttpMethod httpMethod() {
        return HttpMethod.GET;
    }

    @Override
    public List<Param> params() {
        return Lists.newArrayList(
                new Param(DEFAULT_COUNT_KEY, MERGE_FIELDS_COUNT)
        );
    }
}
