package ai.distil.integration.job.sync.parser;

import ai.distil.integration.controller.dto.data.DatasetRow;
import ai.distil.integration.job.sync.AbstractConnection;
import ai.distil.integration.job.sync.holder.DataSourceDataHolder;
import ai.distil.integration.job.sync.iterator.IRowIterator;

import java.util.function.BiConsumer;

public class SimpleParser extends AbstractParser {

    public SimpleParser(AbstractConnection connection, DataSourceDataHolder dataSourceDataHolders) {
        super(connection, dataSourceDataHolders);
    }

    @Override
    public void parse(BiConsumer<DataSourceDataHolder, DatasetRow> callback) {
        IRowIterator iterator = connection.getIterator(this.dataSourceDataHolder);
        iterator.forEachRemaining(r -> callback.accept(this.dataSourceDataHolder, r));
    }

}
