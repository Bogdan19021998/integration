package ai.distil.integration.job.sync.parser;

import ai.distil.integration.controller.dto.data.DatasetRow;
import ai.distil.integration.job.sync.AbstractConnection;
import ai.distil.integration.job.sync.holder.DataSourceDataHolder;
import ai.distil.integration.job.sync.jdbc.SimpleDataSourceDefinition;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Optional;
import java.util.function.BiConsumer;

@AllArgsConstructor
public abstract class AbstractParser {
    @Getter
    protected AbstractConnection connection;
    @Getter
    protected DataSourceDataHolder dataSourceDataHolders;


    public abstract void parse(BiConsumer<DataSourceDataHolder, DatasetRow> callback);

    public DataSourceDataHolder getSchema() {
        return dataSourceDataHolders;
    }

    public DataSourceDataHolder refreshSchema() {
        DataSourceDataHolder currentSchema = this.getSchema();

        return Optional.ofNullable(connection.getDataSource(new SimpleDataSourceDefinition(null, currentSchema.getDataSourceId(), null, null)))
                .map(DataSourceDataHolder::mapFromDTODataSourceEntity)
                .orElse(null);
    }

}
