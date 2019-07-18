package ai.distil.integration.job.sync.iterator;

import ai.distil.integration.controller.dto.data.DatasetPage;
import ai.distil.integration.controller.dto.data.DatasetPageRequest;
import ai.distil.integration.controller.dto.data.DatasetRow;
import ai.distil.integration.job.sync.holder.DataSourceDataHolder;
import ai.distil.integration.job.sync.http.AbstractHttpConnection;
import lombok.RequiredArgsConstructor;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicInteger;

@RequiredArgsConstructor
public class HttpPaginationRowIterator implements IRowIterator {

    private boolean allRetrieved = false;
    private String nextPageUrl;
    private Iterator<DatasetRow> buffer;

    private final AbstractHttpConnection connection;
    private final DataSourceDataHolder dataSourceHolder;
    private final AtomicInteger currentPage;
    private final int defaultPageSize;


    @Override
    public boolean hasNext() {
        if (buffer == null  || (!buffer.hasNext()) && !allRetrieved) {
            fillNextBuffer();
        }
        return buffer.hasNext();
    }

    @Override
    public DatasetRow next() {
        return this.buffer.next();
    }

    @Override
    public DataSourceDataHolder getDataSource() {
        return dataSourceHolder;
    }

    @Override
    public void close() throws Exception {

    }

    private void fillNextBuffer() {
        DatasetPageRequest page = new DatasetPageRequest(currentPage.get(), defaultPageSize, nextPageUrl);

        DatasetPage nextPage = connection.getNextPage(dataSourceHolder, page);

        this.nextPageUrl = nextPage.getNextPageUrl();
        this.buffer = nextPage.getRows().iterator();

        allRetrieved = nextPage.getRows().size() <= defaultPageSize || this.nextPageUrl == null;
        currentPage.incrementAndGet();
    }

}
