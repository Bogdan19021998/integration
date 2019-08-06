package ai.distil.integration.job.sync.parser;

import ai.distil.integration.controller.dto.data.DatasetRow;
import ai.distil.integration.job.sync.AbstractConnection;
import ai.distil.integration.job.sync.holder.DataSourceDataHolder;
import ai.distil.integration.job.sync.jdbc.SimpleDataSourceDefinition;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;
import java.util.function.BiConsumer;

@Slf4j
@AllArgsConstructor
public abstract class AbstractParser {
    @Getter
    protected AbstractConnection connection;
    @Getter
    protected DataSourceDataHolder dataSourceDataHolder;


    public abstract void parse(BiConsumer<DataSourceDataHolder, DatasetRow> callback);

    public DataSourceDataHolder getSchema() {
        return dataSourceDataHolder;
    }

    public DataSourceDataHolder refreshSchema() {
        DataSourceDataHolder currentSchema = this.getSchema();

        return Optional.ofNullable(connection.getDataSource(new SimpleDataSourceDefinition(null, currentSchema.getDataSourceId(), null, null)))
                .map(d -> {
                    if(connection.isDataSourceEligible(d)) {
                        return DataSourceDataHolder.mapFromDTODataSourceEntity(d);
                    }
                    log.info("Looks like datasource - {} is not eligible anymore", currentSchema.getDataSourceId());
                    return null;
                })
                .orElseGet(() -> {
                    log.info("Can't get datasource - {} probably table was removed", currentSchema.getDataSourceId());
                    return null;
                });
    }

}
