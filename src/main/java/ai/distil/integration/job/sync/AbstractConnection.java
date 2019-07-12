package ai.distil.integration.job.sync;

import ai.distil.api.internal.model.dto.DTOConnection;
import ai.distil.api.internal.model.dto.DTODataSource;
import ai.distil.integration.job.sync.holder.DataSourceDataHolder;
import ai.distil.integration.job.sync.iterator.IRowIterator;
import ai.distil.integration.job.sync.jdbc.SimpleDataSourceDefinition;
import lombok.Getter;

import java.util.List;

// it's better to keep connections stateless, these connections not related to jdbc connections
// but for some cases (e.g. ssh port forwarding), connections will be stateful and we'll to close them, for release ports
// or any other resources
public abstract class AbstractConnection implements AutoCloseable {

    //    this is cassandra pattern
    private static final String NAMING_PATTERN = "[^a-zA-Z_0-9]+";

    private static final String TABLE_NAME_PREFIX = "t";
    private static final String COLUMN_NAME_PREFIX = "c";

    @Getter
//  this must be private, for simplify delegation and allow access only by getter
    private DTOConnection connectionData;

    public AbstractConnection(DTOConnection connectionData) {
        this.connectionData = connectionData;
    }

    public abstract boolean isAvailable();

    public abstract List<DTODataSource> getAllDataSources();

    public abstract DTODataSource getDataSource(SimpleDataSourceDefinition tableDefinition);

    public abstract IRowIterator getIterator(DataSourceDataHolder dataSources);

    public abstract boolean dataSourceExist(DataSourceDataHolder dataSource);

    protected String generateTableName(String sourceTable) {
//        todo think about more clever solution
//        1. remove not eligible characters like _,%,-,$, etc... may be a problem for case when the user has several similar tables like
//        v-distil-consumers, v_distil_consumers
//        hash code works fine for such case
        return TABLE_NAME_PREFIX + "_" + sourceTable.replaceAll(NAMING_PATTERN, "") + "_" + Math.abs(sourceTable.hashCode());
    }

    protected String generateColumnName(String sourceColumnName) {
//      todo same as with table names, check above comment
        return COLUMN_NAME_PREFIX + "_" + sourceColumnName.replaceAll(NAMING_PATTERN, "") + "_" + Math.abs(sourceColumnName.hashCode());
    }


}
