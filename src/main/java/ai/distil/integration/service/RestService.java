package ai.distil.integration.service;

import ai.distil.integration.job.sync.http.IDataConverter;
import ai.distil.integration.job.sync.http.request.IHttpRequest;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.asynchttpclient.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import static java.util.Optional.ofNullable;

@Slf4j
@Service
public class RestService {
    private AsyncHttpClient httpClient;

    @PostConstruct
    public void init() {
        this.httpClient = buildHttpClient();
    }

    public <T> T execute(String baseUrl, IHttpRequest<T> request, IDataConverter dataConverter) {
        Request baseRequest = this.getBaseRequest(baseUrl, request.httpMethod().name(), request.urlPart(), request.headers(), request.params(), request.body());
        return execute(baseRequest, dataConverter, request.resultType());
    }

    private  <R> R execute(Request request, IDataConverter dataConverter, TypeReference<R> type) {
        ListenableFuture<Response> responseFuture = this.httpClient.executeRequest(request);

        try {
            Response response = responseFuture.get();
            return ofNullable(HttpStatus.resolve(response.getStatusCode())).map(HttpStatus::is2xxSuccessful)
                    .map(isSuccess -> isSuccess ? dataConverter.fromString(response.getResponseBody(), type) : null)
                    .orElseGet(() -> {
                        log.error("Can't execute http request: {}", request);
                        return null;
                    });

        } catch (ExecutionException | InterruptedException e) {
//          add appropriate message builder
            throw new RuntimeException("Can't execute HTTP request. " + request);
        }
    }

    private Request getBaseRequest(String baseUrl, String method, String urlPart, Map<String, Object> headers, List<Param> params, Object body) {
        RequestBuilder requestBuilder = Dsl.request(method, String.format("%s%s", baseUrl, urlPart));
        headers.forEach(requestBuilder::addHeader);
        requestBuilder.addQueryParams(params);

        return requestBuilder.build();
    }


    protected AsyncHttpClient buildHttpClient() {
        DefaultAsyncHttpClientConfig.Builder clientBuilder = Dsl.config()
                .setConnectTimeout(5000)
                .setReadTimeout(60000)
                .setMaxRedirects(0);

        return Dsl.asyncHttpClient(clientBuilder);
    }

}
