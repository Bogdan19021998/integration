package ai.distil.integration.job.sync.http.request.mailchimp.ingestion;

import ai.distil.integration.job.sync.http.mailchimp.vo.AccountInfo;
import ai.distil.integration.job.sync.http.request.mailchimp.AbstractMailChimpRequest;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.http.HttpMethod;

public class RetrieveAccountInfoMailChimpRequest extends AbstractMailChimpRequest<AccountInfo> {

    public RetrieveAccountInfoMailChimpRequest(String apiKey) {
        super(apiKey);
    }

    @Override
    public TypeReference<AccountInfo> resultType() {
        return new TypeReference<AccountInfo>() {
        };
    }

    @Override
    public String urlPart() {
        return "/";
    }

    @Override
    public HttpMethod httpMethod() {
        return HttpMethod.GET;
    }
}
