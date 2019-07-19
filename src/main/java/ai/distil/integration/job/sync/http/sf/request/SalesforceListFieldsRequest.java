package ai.distil.integration.job.sync.http.sf.request;

import ai.distil.integration.job.sync.http.sf.vo.SalesforceListFields;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.http.HttpMethod;

public class SalesforceListFieldsRequest extends AbstractSalesforceRequest<SalesforceListFields> {

    private String objectName;

    public SalesforceListFieldsRequest(String accessToken, String apiVersion, String objectName) {
        super(accessToken, apiVersion);
        this.objectName = objectName;
    }

    @Override
    public TypeReference<SalesforceListFields> resultType() {
        return new TypeReference<SalesforceListFields>() {};
    }

    @Override
    public String urlPart() {
        return String.format("/services/data/%s/sobjects/%s/describe", this.apiVersion, this.objectName);
    }

    @Override
    public HttpMethod httpMethod() {
        return HttpMethod.GET;
    }
}
