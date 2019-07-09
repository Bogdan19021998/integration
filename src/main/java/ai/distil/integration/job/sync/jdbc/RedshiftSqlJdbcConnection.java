package ai.distil.integration.job.sync.jdbc;

import ai.distil.api.internal.model.dto.DTOConnection;
import ai.distil.integration.configuration.DbConnectionConfiguration;

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

}
