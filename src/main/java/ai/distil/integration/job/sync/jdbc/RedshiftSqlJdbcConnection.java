package ai.distil.integration.job.sync.jdbc;

import ai.distil.api.internal.model.dto.DTOConnection;
import ai.distil.integration.configuration.DbConnectionConfiguration;
import ai.distil.integration.job.sync.jdbc.vo.query.AbstractAllTablesQueryDefinition;
import ai.distil.integration.job.sync.jdbc.vo.query.AbstractDefineSchemaQueryDefinition;
import ai.distil.integration.job.sync.jdbc.vo.query.redshift.AllTablesQueryDefinitionRedshiftSQL;
import ai.distil.integration.job.sync.jdbc.vo.query.redshift.DefineSchemaQueryDefinitionRedshiftSQL;

public class RedshiftSqlJdbcConnection extends PostgreSqlJdbcConnection {

    public RedshiftSqlJdbcConnection(DTOConnection connectionData) {
        super(connectionData);
    }

    @Override
    protected String getProtocol() {
        return DbConnectionConfiguration.REDSHIFT.getProtocol();
    }

    @Override
    protected String getConnectionProperties() {
        return DbConnectionConfiguration.REDSHIFT.getProps();
    }

    @Override
    protected AbstractAllTablesQueryDefinition getAllTablesQuery() {
        return new AllTablesQueryDefinitionRedshiftSQL(getDbName());
    }

    @Override
    protected AbstractDefineSchemaQueryDefinition getDefineSchemaQuery(String tableName) {
        return new DefineSchemaQueryDefinitionRedshiftSQL(getDbName(), tableName);
    }
}
