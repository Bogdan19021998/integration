package ai.distil.integration.job.sync.http;

import ai.distil.api.internal.model.dto.DTOConnection;
import ai.distil.api.internal.model.dto.DTODataSource;
import ai.distil.integration.controller.dto.data.DatasetRow;
import ai.distil.integration.job.sync.AbstractConnection;
import ai.distil.integration.job.sync.holder.DataSourceDataHolder;
import ai.distil.integration.job.sync.iterator.IRowIterator;
import com.fasterxml.jackson.core.type.TypeReference;
import org.asynchttpclient.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static java.util.Optional.ofNullable;

public abstract class AbstractHttpConnection extends AbstractConnection {

    protected static final String AUTH_HEADER_KEY = "Authorization";

    protected AsyncHttpClient httpClient;

    public AbstractHttpConnection(DTOConnection dtoConnection) {
        super(dtoConnection);
        this.httpClient = buildHttpClient();
    }

    @Override
    public List<DTODataSource> getAllDataSources() {
        return null;
    }

    @Override
    public IRowIterator getIterator(DataSourceDataHolder dataSources) {
        return null;
    }

    @Override
    public boolean dataSourceExist(DataSourceDataHolder dataSource) {
        return false;
    }

    @Override
    public void close() throws Exception {
//        do nothing
    }

    protected AsyncHttpClient buildHttpClient() {
        DefaultAsyncHttpClientConfig.Builder clientBuilder = Dsl.config()
                .setConnectTimeout(5000)
                .setReadTimeout(60000)
                .setMaxRedirects(0);

        return Dsl.asyncHttpClient(clientBuilder);
    }

    public abstract List<DatasetRow> getNextPage(DTODataSource dataSource, PageRequest pageRequest);

    protected abstract String getBaseUrl();

    protected <R> R execute(Request request, TypeReference<R> type) {
        ListenableFuture<Response> responseFuture = this.httpClient.executeRequest(request);

        try {
            Response response = responseFuture.get();
            return ofNullable(HttpStatus.resolve(response.getStatusCode())).map(HttpStatus::is2xxSuccessful)
                    .map(isSuccess -> isSuccess ? this.getDataConverter().fromString(response.getResponseBody(), type) : null)
                    .orElse(null);

        } catch (ExecutionException | InterruptedException e) {
//          add appropriate message builder
            throw new RuntimeException("Can't execute HTTP request. " + request);
        }

    }

    protected Request getBaseGetRequest(String urlPart) {
        return getBaseGetRequest(urlPart, Collections.emptyList());
    }

    protected Request getBaseGetRequest(String urlPart, List<Param> params) {
        return getBaseGetRequest(urlPart, Collections.emptyMap(), params);
    }

    protected Request getBaseGetRequest(String urlPart, Map<String, String> headers, List<Param> params) {
        return getBaseRequest("GET", urlPart, headers, params, null);
    }

    protected Request getBasePostRequest(String urlPart, Map<String, String> headers, List<Param> params, Object body) {
        return getBaseRequest("POST", urlPart, headers, params, body);
    }

    protected Map<String, Object> getDefaultHeaders() {
        return Collections.emptyMap();
    }

    protected abstract IDataConverter getDataConverter();

    private Request getBaseRequest(String method, String urlPart, Map<String, String> headers, List<Param> params, Object body) {
        RequestBuilder requestBuilder = Dsl.request(method, String.format("%s%s", getBaseUrl(), urlPart));
        headers.forEach(requestBuilder::addHeader);
        requestBuilder.addQueryParams(params);

        getDefaultHeaders().forEach(requestBuilder::addHeader);

        return requestBuilder.build();
    }


}
