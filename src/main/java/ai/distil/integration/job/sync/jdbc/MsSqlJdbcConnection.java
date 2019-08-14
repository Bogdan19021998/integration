package ai.distil.integration.job.sync.jdbc;

import ai.distil.api.internal.model.dto.DTOConnection;
import ai.distil.integration.configuration.DbConnectionConfiguration;
import ai.distil.integration.job.sync.jdbc.vo.query.AbstractAllTablesQueryDefinition;
import ai.distil.integration.job.sync.jdbc.vo.query.AbstractDefineSchemaQueryDefinition;
import ai.distil.integration.job.sync.jdbc.vo.query.mssql.AllTablesQueryDefinitionMsSQL;
import ai.distil.integration.job.sync.jdbc.vo.query.mssql.DefineSchemaQueryDefinitionMsSQL;
import ai.distil.model.org.ConnectionSettings;

public class MsSqlJdbcConnection extends JdbcConnection {

    public MsSqlJdbcConnection(DTOConnection connectionData) {
        super(connectionData);
    }

    @Override
    protected AbstractDefineSchemaQueryDefinition getDefineSchemaQuery(String tableName) {
        ConnectionSettings connectionSettings = getConnectionSettings();
        return new DefineSchemaQueryDefinitionMsSQL(connectionSettings.getSchema(), tableName);
    }

    @Override
    protected AbstractAllTablesQueryDefinition getAllTablesQuery() {
        String schema = getConnectionSettings().getSchema();
        return new AllTablesQueryDefinitionMsSQL(schema);
    }

    @Override
    protected String getProtocol() {
        return DbConnectionConfiguration.MS_SQL.getProtocol();
    }

    @Override
    protected String getConnectionProperties() {
        return DbConnectionConfiguration.MS_SQL.getProps();
    }

    @Override
    protected String getQuoteSymbol() {
        return "";
    }

    @Override
    protected String getConnectionString() {
        ConnectionSettings connectionSettings = this.getConnectionSettings();
        String address = connectionSettings.getServerAddress();
        String port = connectionSettings.getPort();
        String databaseName = connectionSettings.getDatabaseName();

        return String.format("jdbc:%s://%s:%s;databaseName=%s", getProtocol(), address, port, databaseName);
    }

    @Override
    protected String getTableName(String tableName) {
        String schema = getConnectionSettings().getSchema();
        return quoteString(schema + "." + tableName);
    }

    @Override
    public void close() throws Exception {
//        there is nothing to close, default implementation is stateless
    }
}
