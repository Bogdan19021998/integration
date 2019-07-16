package ai.distil.integration.job.sync.iterator;

import ai.distil.integration.controller.dto.data.DatasetRow;
import ai.distil.integration.job.sync.holder.DataSourceDataHolder;
import ai.distil.integration.job.sync.http.AbstractHttpConnection;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@RequiredArgsConstructor
public class HttpPaginationRowIterator implements IRowIterator {

    private boolean allRetrieved = false;

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
        return null;
    }

    @Override
    public void close() throws Exception {

    }

    private void fillNextBuffer() {
        PageRequest page = PageRequest.of(currentPage.get(), defaultPageSize);
        List<DatasetRow> nextPage = connection.getNextPage(dataSourceHolder, page);

        this.buffer = nextPage.iterator();

        allRetrieved = nextPage.size() != defaultPageSize;
        currentPage.incrementAndGet();
    }

}
