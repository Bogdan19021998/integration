package ai.distil.integration.job.sync.http.mailchimp;

import ai.distil.api.internal.model.dto.DTOConnection;
import ai.distil.api.internal.model.dto.DTODataSource;
import ai.distil.integration.configuration.HttpConnectionConfiguration;
import ai.distil.integration.job.sync.http.AbstractHttpConnection;
import ai.distil.integration.job.sync.http.IDataConverter;
import ai.distil.integration.job.sync.http.JsonDataConverter;
import ai.distil.integration.job.sync.http.mailchimp.vo.Audience;
import ai.distil.integration.job.sync.http.mailchimp.vo.ResponseWrapper;
import ai.distil.model.org.ConnectionSettings;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import org.asynchttpclient.Request;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MailChimpHttpConnection extends AbstractHttpConnection {


    private static final String LISTS_URL = "/lists";
    private static final TypeReference<ResponseWrapper<Audience>> AUDIENCE_TYPE_REFERENCE = new TypeReference<ResponseWrapper<Audience>>() {};


    public MailChimpHttpConnection(DTOConnection dtoConnection) {
        super(dtoConnection);
    }

    @Override
    public List<DTODataSource> getAllDataSources() {
        Request request = getBaseGetRequest(LISTS_URL);
        ResponseWrapper<Audience> result = execute(request, AUDIENCE_TYPE_REFERENCE);
        return Lists.newArrayList();
    }

    @Override
    protected Map<String, Object> getDefaultHeaders() {
        Map<String, Object> headers = new HashMap<>(super.getDefaultHeaders());

        String apiKey = Optional.ofNullable(this.getConnectionData())
                .map(DTOConnection::getConnectionSettings)
                .map(ConnectionSettings::getApiKey)
                .orElse(null);

        headers.put(AUTH_HEADER_KEY, String.format("Basic %s", apiKey));

        return headers;
    }

    @Override
    protected IDataConverter getDataConverter() {
        return JsonDataConverter.getInstance();
    }

    @Override
    protected String getBaseUrl() {
        return HttpConnectionConfiguration.MAIL_CHIMP.getBaseUrl();
    }

}
