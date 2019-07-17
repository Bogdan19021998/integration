package ai.distil.integration.job.sync.http.sf.request;

import ai.distil.integration.job.sync.http.request.IHttpRequest;
import ai.distil.integration.job.sync.http.sf.vo.SalesforceLoginResponse;
import ai.distil.model.org.ConnectionSettings;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.ImmutableMap;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpMethod;

@AllArgsConstructor
public class SalesforceLoginRequest implements IHttpRequest<SalesforceLoginResponse> {

    private ConnectionSettings settings;

    @Override
    public TypeReference<SalesforceLoginResponse> resultType() {
        return new TypeReference<SalesforceLoginResponse>() {};
    }

    @Override
    public String urlPart() {
        String requestParams = buildRequestParams(ImmutableMap.of(
                "grant_type", "password",
                "client_id", this.settings.getApiKey(),
                "client_secret", this.settings.getClientSecret(),
                "username", this.settings.getUserName(),
                "password", this.settings.getPassword() + this.settings.getSecurityCode()
        ));

        return String.format("/services/oauth2/token%s", requestParams);
    }

    @Override
    public HttpMethod httpMethod() {
        return HttpMethod.POST;
    }
}
