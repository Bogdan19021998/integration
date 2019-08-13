package ai.distil.integration.job.sync;

import ai.distil.api.internal.model.dto.DTOConnection;
import ai.distil.api.internal.model.dto.DTODataSource;
import ai.distil.api.internal.model.dto.DTODataSourceAttribute;
import ai.distil.integration.job.sync.holder.DataSourceDataHolder;
import ai.distil.integration.job.sync.http.mailchimp.SimpleDataSourceField;
import ai.distil.integration.job.sync.iterator.IRowIterator;
import ai.distil.integration.job.sync.jdbc.SimpleDataSourceDefinition;
import ai.distil.integration.utils.NamingUtils;
import ai.distil.model.org.ConnectionSettings;
import ai.distil.model.types.CassandraDataSourceAttributeType;
import ai.distil.model.types.DataSourceSchemaAttributeTag;
import com.google.common.collect.Sets;
import lombok.Getter;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static ai.distil.model.types.DataSourceSchemaAttributeTag.*;

// it's better to keep connections stateless, these connections not related to jdbc connections
// but for some cases (e.g. ssh port forwarding), connections will be stateful and we'll to close them, for release ports
// or any other resources
public abstract class AbstractConnection implements AutoCloseable {

    private static final Set<DataSourceSchemaAttributeTag> PRIMARY_KEY_ATTRIBUTES = Sets.newHashSet(
            CONTENT_EXTERNAL_ID, CUSTOMER_EXTERNAL_ID,
            ORDER_EXTERNAL_ID, PRODUCT_EXTERNAL_ID,
            PRIMARY_KEY
    );

    @Getter
//  this must be private, for simplify delegation and allow access only by getter
    private DTOConnection connectionData;

    public AbstractConnection(DTOConnection connectionData) {
        this.connectionData = connectionData;
    }

    public abstract boolean isAvailable();

    public abstract List<DTODataSource> getAllDataSources();

    protected List<DTODataSource> filterEligibleDataSource(List<DTODataSource> dataSources) {
        return dataSources;
    }

    public boolean isDataSourceEligible(DTODataSource dataSource) {
        return this.filterEligibleDataSource(Collections.singletonList(dataSource)).size() == 1;
    }

    public List<DTODataSource> getEligibleDataSources() {
        return filterEligibleDataSource(getAllDataSources());
    }

    public abstract DTODataSource getDataSource(SimpleDataSourceDefinition sourceDefinition);

    public abstract IRowIterator getIterator(DataSourceDataHolder dataSources);

    public abstract boolean dataSourceExist(DataSourceDataHolder dataSource);

    protected ConnectionSettings getConnectionSettings() {
        return this.getConnectionData().getConnectionSettings();
    }

    protected String generateTableName(String sourceTable) {
        return NamingUtils.generateTableName(sourceTable);
    }

    protected String generateColumnName(String sourceColumnName) {
        return NamingUtils.generateColumnName(sourceColumnName);
    }

    protected DTODataSourceAttribute buildDTODataSourceAttribute(SimpleDataSourceField field) {
//      key attributes must be strings
        CassandraDataSourceAttributeType cassandraType = PRIMARY_KEY_ATTRIBUTES.contains(field.getAttributeTag())
                ? CassandraDataSourceAttributeType.STRING
                : field.getAttributeType();

        return new DTODataSourceAttribute(null,
                field.getSourceFieldName(),
                field.getDisplayName(),
                generateColumnName(field.getSourceFieldName()),
                cassandraType.getAttributeType(),
                cassandraType,
                false,
                field.getAttributeTag(),
                true,
                new Date(),
                new Date(),
                null);
    }

}
