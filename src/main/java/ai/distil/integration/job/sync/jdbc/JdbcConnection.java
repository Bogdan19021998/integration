package ai.distil.integration.job.sync.jdbc;

import ai.distil.api.internal.model.dto.DTOConnection;
import ai.distil.api.internal.model.dto.DTODataSource;
import ai.distil.api.internal.model.dto.DTODataSourceAttribute;
import ai.distil.integration.job.sync.AbstractConnection;
import ai.distil.integration.job.sync.SyncTableDefinition;
import ai.distil.integration.job.sync.holder.DataSourceDataHolder;
import ai.distil.integration.job.sync.iterator.IRowIterator;
import ai.distil.integration.job.sync.iterator.JdbcRowIterator;
import ai.distil.integration.job.sync.jdbc.vo.ColumnDefinition;
import ai.distil.integration.job.sync.jdbc.vo.QueryWrapper;
import ai.distil.integration.job.sync.jdbc.vo.query.AbstractAllTablesQueryDefinition;
import ai.distil.integration.job.sync.jdbc.vo.query.AbstractDefineSchemaQueryDefinition;
import ai.distil.integration.job.sync.jdbc.vo.query.SimpleCheckDataSourceExistingQueryDefinition;
import ai.distil.model.org.ConnectionSettings;
import ai.distil.model.org.SyncSchedule;
import ai.distil.model.types.SyncFrequency;
import org.apache.commons.dbutils.DbUtils;
import org.hibernate.JDBCException;
import org.hibernate.exception.JDBCConnectionException;

import java.sql.*;
import java.util.Date;
import java.util.*;
import java.util.stream.Collectors;

public abstract class JdbcConnection extends AbstractConnection {

    private static final Integer DEFAULT_FETCH_SIZE = 10000;

    public JdbcConnection(DTOConnection connectionData) {
        super(connectionData);
    }

    @Override
    public boolean isAvailable() {
        return getConnection(true) != null;
    }

    @Override
    public IRowIterator getIterator(DataSourceDataHolder dataSource) {
        return new JdbcRowIterator(this, dataSource);
    }

    protected abstract AbstractDefineSchemaQueryDefinition getDefineSchemaQuery(String tableName);

    protected abstract AbstractAllTablesQueryDefinition getAllTablesQuery();

    @Override
    protected List<DTODataSource> filterEligibleDataSource(List<DTODataSource> dataSources) {

        return dataSources.stream()
                .filter(dataSource -> SyncTableDefinition.defineSyncTableDefinition(dataSource.getDataSourceType())
                        .map(s -> s.isDataSourceEligible(dataSource))
                        .orElse(false))
                .collect(Collectors.toList());
    }

    @Override
    public boolean dataSourceExist(DataSourceDataHolder dataSource) {
        SimpleCheckDataSourceExistingQueryDefinition queryDef = new SimpleCheckDataSourceExistingQueryDefinition(getConnectionData().getConnectionSettings().getSchema(), dataSource.getDataSourceId());
        String query = queryDef.getQuery();
        try (QueryWrapper queryWrapper = this.query(query, queryDef.getQueryParams())) {
            return queryDef.mapResultSet(queryWrapper.getResultSet());
        } catch (Exception e) {
//          todo that is pretty simple check, I think each db may return the special type of exception
//          check it after connecting other dbs
            return false;
        }
    }

    @Override
    public DTODataSource getDataSource(SimpleDataSourceDefinition tableDefinition) {
        String sourceTableName = tableDefinition.getDataSourceId();

        Optional<SyncTableDefinition> syncTableDefinition = SyncTableDefinition.identifySyncTableDefinition(sourceTableName);

        AbstractDefineSchemaQueryDefinition schemaQueryDefinition = getDefineSchemaQuery(sourceTableName);
        List<DTODataSourceAttribute> columns = new ArrayList<>();
        String sql = schemaQueryDefinition.getQuery();

        try (QueryWrapper query = this.query(sql, schemaQueryDefinition.getQueryParams(), false)) {

            ResultSet resultSet = query.getResultSet();
            while (resultSet.next()) {
                ColumnDefinition columnDefinition = schemaQueryDefinition.mapResultSet(resultSet);
                String sourceColumnName = columnDefinition.getColumnName();

                String distilColumnName = generateColumnName(sourceColumnName);

                columns.add(new DTODataSourceAttribute(null,
                        sourceColumnName,
                        sourceColumnName,
                        distilColumnName,
                        columnDefinition.getDataType(),
                        false,
                        syncTableDefinition.map(v -> v.tryToGetAttributeType(sourceColumnName)).orElse(null),
                        true,
                        new Date(),
                        new Date()));
            }
        } catch (SQLException e) {
            throw new JDBCException("Can't retrieve all MySQL tables.", e, sql);
        }

//todo fix this
        SyncSchedule syncSchedule = new SyncSchedule();
        syncSchedule.setSyncFrequency(SyncFrequency.daily);

        return new DTODataSource(null,
                this.getConnectionData().getId(),
                sourceTableName,
                tableDefinition.getDescription(),
                sourceTableName,
                syncSchedule,
                new Date(),
                null,
                syncTableDefinition.map(SyncTableDefinition::getDataSourceType).orElse(null),
                null,
                null,
                columns,
                generateTableName(sourceTableName),
                null
        );
    }

    //  should returns all tables if pass null or empty list
    public List<SimpleDataSourceDefinition> getAllTables() {
        AbstractAllTablesQueryDefinition allTablesQuery = getAllTablesQuery();
        List<SimpleDataSourceDefinition> result = new ArrayList<>();
        String sql = allTablesQuery.getQuery();

        try (QueryWrapper query = this.query(sql, allTablesQuery.getQueryParams(), false)) {

            ResultSet resultSet = query.getResultSet();
            while (resultSet.next()) {
                result.add(allTablesQuery.mapResultSet(resultSet));
            }
        } catch (SQLException e) {
            throw new JDBCException("Can't retrieve all MySQL tables.", e, sql);
        }

        return result;
    }

    public QueryWrapper queryTable(DataSourceDataHolder dataSource) {
        return query(selectAllAvailableFieldsStatement(dataSource));
    }

    @Override
    public List<DTODataSource> getAllDataSources() {
        return getAllTables().stream()
                .map(this::getDataSource)
                .collect(Collectors.toList());
    }

    //    mysql, postgres, etc...
    protected abstract String getProtocol();

    public QueryWrapper query(String query) {
        return query(query, Collections.emptyList());
    }

    public QueryWrapper query(String query, List<Object> params) {
        return query(query, params, false);
    }

    public QueryWrapper execute(String query) {
        return this.query(query, Collections.emptyList(), true);
    }

    //  run any query with params
    protected QueryWrapper query(String query, List<Object> params, boolean withoutResult) {
        QueryWrapper result = new QueryWrapper();
        try {
            Connection connection = this.getConnection(false);
            result.setConnection(connection);

            PreparedStatement statement = connection.prepareStatement(query);
            if(!withoutResult) {
                statement.setFetchSize(DEFAULT_FETCH_SIZE);
            }

            for (int i = 1; i <= params.size(); i++) {
                statement.setObject(i, params.get(i - 1));
            }
            result.setStatement(statement);
            if (withoutResult) {
                statement.execute();
            } else {
                ResultSet resultSet = statement.executeQuery();
                result.setResultSet(resultSet);
            }
        } catch (SQLException e) {
            result.close();
            throw new JDBCException("Can't execute query. ", e, query);
        }
        return result;
    }

    protected abstract String getConnectionString();

    protected Properties getProperties() {
        ConnectionSettings connectionSettings = this.getConnectionSettings();
        Properties props = new Properties();
        Optional.ofNullable(connectionSettings.getUserName()).ifPresent(v -> props.setProperty("user", v));
        Optional.ofNullable(connectionSettings.getPassword()).ifPresent(v -> props.setProperty("password", v));
        return props;
    }

    protected String quoteString(String str) {
        String quoteSymbol = getQuoteSymbol();
        return quoteSymbol + str + quoteSymbol;
    }

    //  jdbc url properties
    protected abstract String getConnectionProperties();

    //  quote symbol for special characters
    protected abstract String getQuoteSymbol();

    protected String getTableName(String tableName) {
        return quoteString(tableName);
    }

    private String selectAllAvailableFieldsStatement(DataSourceDataHolder dataSource) {
        String fieldsList = dataSource.getAllAttributes().stream()
                .map(attr -> String.format("%1$s as %1$s", quoteString(attr.getAttributeSourceName())))
                .collect(Collectors.joining(", "));

        return String.format("SELECT %s FROM %s.%s",
                fieldsList,
                quoteString(getConnectionData().getConnectionSettings().getSchema()),
                quoteString(dataSource.getDataSourceId()));
    }

    private Connection getConnection(boolean close) {
        Connection connection = null;

        try {
            Properties properties = getProperties();
            connection = DriverManager.getConnection(getConnectionString(), properties);
        } catch (SQLException e) {
            throw new JDBCConnectionException("Can't connect to datasource.", e);
        } finally {
            if (close) {
                DbUtils.closeQuietly(connection);
            }
        }
        return connection;
    }

}
