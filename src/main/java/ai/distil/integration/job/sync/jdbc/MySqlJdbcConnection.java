package ai.distil.integration.job.sync.jdbc;

import ai.distil.api.internal.model.dto.DTOConnection;
import ai.distil.integration.configuration.DbConnectionConfiguration;
import ai.distil.integration.job.sync.jdbc.vo.query.AbstractAllTablesQueryDefinition;
import ai.distil.integration.job.sync.jdbc.vo.query.AbstractDefineSchemaQueryDefinition;
import ai.distil.integration.job.sync.jdbc.vo.query.mysql.AllTablesQueryDefinitionMySQL;
import ai.distil.integration.job.sync.jdbc.vo.query.mysql.DefineSchemaQueryDefinitionMySQL;
import ai.distil.model.org.ConnectionSettings;

public class MySqlJdbcConnection extends JdbcConnection {

    public MySqlJdbcConnection(DTOConnection connectionData) {
        super(connectionData);
    }

    @Override
    protected AbstractDefineSchemaQueryDefinition getDefineSchemaQuery(String tableName) {
        ConnectionSettings connectionSettings = this.getConnectionSettings();
        return new DefineSchemaQueryDefinitionMySQL(connectionSettings.getSchema(), tableName);
    }

    @Override
    protected AbstractAllTablesQueryDefinition getAllTablesQuery() {
        String schema = this.getConnectionSettings().getDatabaseName();
        return new AllTablesQueryDefinitionMySQL(schema);
    }

    @Override
    protected String getProtocol() {
        return DbConnectionConfiguration.MY_SQL.getProtocol();
    }

    @Override
    protected String getConnectionString() {
        ConnectionSettings connectionSettings = this.getConnectionSettings();
        String address = connectionSettings.getServerAddress();
        String port = connectionSettings.getPort();
        String dbName = connectionSettings.getDatabaseName();

        return String.format("jdbc:%s://%s:%s/%s%s", getProtocol(), address, port, dbName, getConnectionProperties());
    }

    @Override
    protected String getConnectionProperties() {
        return DbConnectionConfiguration.MY_SQL.getProps();
    }

    @Override
    protected String getQuoteSymbol() {
        return "`";
    }

    @Override
    public void close() throws Exception {
//        there is nothing to close, default implementation is stateless
    }
}
