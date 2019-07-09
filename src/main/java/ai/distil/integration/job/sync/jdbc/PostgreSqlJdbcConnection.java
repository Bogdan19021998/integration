package ai.distil.integration.job.sync.jdbc;

import ai.distil.api.internal.model.dto.DTOConnection;
import ai.distil.integration.configuration.DbConnectionConfiguration;
import ai.distil.integration.job.sync.jdbc.vo.query.AbstractAllTablesQueryDefinition;
import ai.distil.integration.job.sync.jdbc.vo.query.AbstractDefineSchemaQueryDefinition;
import ai.distil.integration.job.sync.jdbc.vo.query.postgresql.AllTablesQueryDefinitionPostgreSQL;
import ai.distil.integration.job.sync.jdbc.vo.query.postgresql.DefineSchemaQueryDefinitionPostgreSQL;
import ai.distil.model.org.ConnectionSettings;

public class PostgreSqlJdbcConnection extends JdbcConnection {

    public PostgreSqlJdbcConnection(DTOConnection connectionData) {
        super(connectionData);
    }

    @Override
    protected AbstractDefineSchemaQueryDefinition getDefineSchemaQuery(String tableName) {
        ConnectionSettings connectionSettings = this.getConnectionData().getConnectionSettings();
        return new DefineSchemaQueryDefinitionPostgreSQL(connectionSettings.getSchema(), tableName);
    }

    @Override
    protected AbstractAllTablesQueryDefinition getAllTablesQuery() {
        String schema = this.getConnectionData().getConnectionSettings().getSchema();
        return new AllTablesQueryDefinitionPostgreSQL(schema);
    }

    @Override
    protected String getProtocol() {
        return DbConnectionConfiguration.POSTGRE_SQL.getProtocol();
    }

    @Override
    protected String getConnectionString() {
        ConnectionSettings connectionSettings = this.getConnectionData().getConnectionSettings();
        String address = connectionSettings.getServerAddress();
        String port = connectionSettings.getPort();
        String schema = connectionSettings.getSchema();

        return String.format("jdbc:%s://%s:%s/%s?currentSchema=%s", getProtocol(), address, port, connectionSettings.getDatabaseName(), schema);
    }

    @Override
    protected String getConnectionProperties() {
        return DbConnectionConfiguration.POSTGRE_SQL.getProps();
    }

    @Override
    protected String getQuoteSymbol() {
        return "\"";
    }

    @Override
    public void close() throws Exception {
//        there is nothing to close, default implementation is stateless
    }
}
