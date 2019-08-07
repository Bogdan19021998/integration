package ai.distil.integration.job.sync.http;

import ai.distil.api.internal.model.dto.DTOConnection;
import ai.distil.api.internal.model.dto.DTODataSourceAttribute;
import ai.distil.integration.controller.dto.data.DatasetPage;
import ai.distil.integration.controller.dto.data.DatasetPageRequest;
import ai.distil.integration.job.sync.AbstractConnection;
import ai.distil.integration.job.sync.holder.DataSourceDataHolder;
import ai.distil.integration.job.sync.http.mailchimp.SimpleDataSourceField;
import ai.distil.integration.job.sync.iterator.HttpPaginationRowIterator;
import ai.distil.integration.job.sync.iterator.IRowIterator;
import ai.distil.integration.service.RestService;

import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class AbstractHttpConnection extends AbstractConnection {
    protected RestService restService;
    protected IFieldsHolder<?> fieldsHolder;

    public AbstractHttpConnection(DTOConnection dtoConnection, RestService restService, IFieldsHolder fieldsHolder) {
        super(dtoConnection);
        this.restService = restService;
        this.fieldsHolder = fieldsHolder;
    }

    @Override
    public IRowIterator getIterator(DataSourceDataHolder dataSource) {
        return new HttpPaginationRowIterator(this, dataSource, new AtomicInteger(getDefaultPageNumber()), getDefaultPageSize());
    }

    @Override
    public void close() throws Exception {
//        do nothing
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

    protected DTODataSourceAttribute buildDTODataSourceAttribute(SimpleDataSourceField field) {
        return new DTODataSourceAttribute(null,
                field.getSourceFieldName(),
                field.getDisplayName(),
                generateColumnName(field.getSourceFieldName()),
                field.getAttributeType(),
                false,
                field.getAttributeTag(),
                true,
                new Date(),
                new Date(),
                null);
    }


}
