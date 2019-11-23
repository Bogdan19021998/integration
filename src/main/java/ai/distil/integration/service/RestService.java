package ai.distil.integration.service;

import ai.distil.integration.job.sync.http.IDataConverter;
import ai.distil.integration.job.sync.http.request.IHttpRequest;
import lombok.extern.slf4j.Slf4j;
import org.asynchttpclient.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

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
        String body = bodyToString(dataConverter, request.getBody());
        Request baseRequest = this.getBaseRequest(baseUrl, request.httpMethod().name(), request.urlPart(), request.headers(), request.params(), body);

        return execute(baseRequest, dataConverter, request);
    }

    public <R, T> CompletableFuture<R> executeAsync(String baseUrl, IHttpRequest<T> request, IDataConverter dataConverter,
                                 Function<T, R> f) {
        CompletableFuture<T> responseFuture = executeAsync(baseUrl, request, dataConverter);
        return responseFuture.thenApply(f);

    }

    public <T> CompletableFuture<T> executeAsync(String baseUrl, IHttpRequest<T> request, IDataConverter dataConverter) {
        String body = bodyToString(dataConverter, request.getBody());
        Request baseRequest = this.getBaseRequest(baseUrl, request.httpMethod().name(), request.urlPart(), request.headers(), request.params(), body);

        return this.httpClient.executeRequest(baseRequest)
                .toCompletableFuture()
                .thenApply(response -> ofNullable(HttpStatus.resolve(response.getStatusCode())).map(HttpStatus::is2xxSuccessful)
                .map(isSuccess -> {
                    if(isSuccess) {
                        return dataConverter.fromString(response.getResponseBody(), request.resultType());
                    }
                    request.handleError(response, dataConverter);
                    return null;
                })
                .orElse(null));
    }

    private <R> R execute(Request request, IDataConverter dataConverter, IHttpRequest<R> httpRequest) {
        ListenableFuture<Response> responseFuture = this.httpClient.executeRequest(request);

        try {
            Response response = responseFuture.get();
            if(HttpStatus.resolve(response.getStatusCode()).is2xxSuccessful()) {
                return dataConverter.fromString(response.getResponseBody(), httpRequest.resultType());
            } else {
                log.error("Can't execute http request: {}, error: {}", request, response.getResponseBody());
//                        may throw exception if it's real error
                httpRequest.handleError(response, dataConverter);
                return null;
            }

        } catch (ExecutionException | InterruptedException e) {
//          add appropriate message builder
            throw new RuntimeException("Can't execute HTTP request. " + request, e);
        }
    }

    private Request getBaseRequest(String baseUrl, String method, String urlPart, Map<String, Object> headers, List<Param> params, String body) {
        RequestBuilder requestBuilder = Dsl.request(method, String.format("%s%s", baseUrl, urlPart));
        headers.forEach(requestBuilder::addHeader);
        requestBuilder.addQueryParams(params);
        requestBuilder.setBody(body);

        return requestBuilder.build();
    }

    private String bodyToString(IDataConverter dataConverter, Object body) {
        return body == null ? null : dataConverter.toString(body);
    }


    protected AsyncHttpClient buildHttpClient() {
        DefaultAsyncHttpClientConfig.Builder clientBuilder = Dsl.config()
                .setConnectTimeout(5000)
                .setReadTimeout(60000)
                .setMaxRedirects(0);

        return Dsl.asyncHttpClient(clientBuilder);
    }

}
