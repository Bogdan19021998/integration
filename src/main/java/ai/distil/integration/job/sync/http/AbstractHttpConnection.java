package ai.distil.integration.job.sync.http;

import ai.distil.api.internal.model.dto.DTOConnection;
import ai.distil.api.internal.model.dto.DTODataSource;
import ai.distil.integration.controller.dto.data.DatasetPage;
import ai.distil.integration.controller.dto.data.DatasetPageRequest;
import ai.distil.integration.job.sync.AbstractConnection;
import ai.distil.integration.job.sync.holder.DataSourceDataHolder;
import ai.distil.integration.job.sync.http.request.IHttpRequest;
import ai.distil.integration.job.sync.iterator.HttpPaginationRowIterator;
import ai.distil.integration.job.sync.iterator.IRowIterator;
import ai.distil.integration.service.RestService;
import ai.distil.model.org.ConnectionSettings;
import lombok.Getter;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class AbstractHttpConnection extends AbstractConnection {
    @Getter
    protected RestService restService;
    protected IFieldsHolder<?> fieldsHolder;

    public AbstractHttpConnection(DTOConnection dtoConnection, RestService restService, IFieldsHolder fieldsHolder) {
        super(dtoConnection);
        this.restService = restService;
        this.fieldsHolder = fieldsHolder;
    }

    @Override
    protected List<DTODataSource> filterEligibleDataSource(List<DTODataSource> dataSources) {
        return dataSources;
    }

    @Override
    public IRowIterator getIterator(DataSourceDataHolder dataSource) {
        return new HttpPaginationRowIterator(this, dataSource, new AtomicInteger(getDefaultPageNumber()), getDefaultPageSize());
    }

    @Override
    public void close() throws Exception {
//        do nothing
    }

    public <T> T executeRequest(IHttpRequest<T> r) {
        return this.restService.execute(getBaseUrl(), r, JsonDataConverter.getInstance());
    }

    public <T> CompletableFuture<T> executeAsyncRequest(IHttpRequest<T> r) {
        return this.restService.executeAsync(getBaseUrl(), r, JsonDataConverter.getInstance());
    }

    public String getApiKey() {
        return Optional.ofNullable(this.getConnectionData())
                .map(DTOConnection::getConnectionSettings)
                .map(ConnectionSettings::getApiKey)
                .orElse(null);
    }


    //  for some sources, page numbers starting from 1, weird things happen
    protected int getDefaultPageNumber() {
        return 0;
    }

    public abstract DatasetPage getNextPage(DataSourceDataHolder dataSource, DatasetPageRequest pageRequest);

    protected Integer getDefaultPageSize() {
        return 100;
    }

    protected abstract String getBaseUrl();

}
