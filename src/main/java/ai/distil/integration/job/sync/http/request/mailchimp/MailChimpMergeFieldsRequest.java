package ai.distil.integration.job.sync.http.request.mailchimp;

import com.fasterxml.jackson.core.type.TypeReference;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpMethod;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MailChimpMergeFieldsRequest implements IMailChimpRequest<Map<String, Object>> {

    private String listId;

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
}
