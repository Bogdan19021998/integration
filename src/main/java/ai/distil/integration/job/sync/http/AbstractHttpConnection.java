package ai.distil.integration.job.sync.http;

import ai.distil.api.internal.model.dto.DTOConnection;
import ai.distil.integration.controller.dto.data.DatasetRow;
import ai.distil.integration.job.sync.AbstractConnection;
import ai.distil.integration.job.sync.holder.DataSourceDataHolder;
import ai.distil.integration.job.sync.iterator.HttpPaginationRowIterator;
import ai.distil.integration.job.sync.iterator.IRowIterator;
import ai.distil.integration.service.RestService;
import org.springframework.data.domain.PageRequest;

import java.util.List;

public abstract class AbstractHttpConnection extends AbstractConnection {
    protected RestService restService;

    public AbstractHttpConnection(DTOConnection dtoConnection, RestService restService) {
        super(dtoConnection);
        this.restService = restService;
    }

    @Override
    public IRowIterator getIterator(DataSourceDataHolder dataSources) {
        return new HttpPaginationRowIterator(this, dataSources, getDefaultPageSize());
    }

    @Override
    public void close() throws Exception {
//        do nothing
    }

    public abstract List<DatasetRow> getNextPage(DataSourceDataHolder dataSource, PageRequest pageRequest);

    protected Integer getDefaultPageSize() {
        return 1000;
    }

    protected abstract String getBaseUrl();


}
