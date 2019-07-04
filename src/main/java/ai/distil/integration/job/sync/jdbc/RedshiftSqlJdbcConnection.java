package ai.distil.integration.job.sync.jdbc;

import ai.distil.api.internal.model.dto.DTOConnection;
import ai.distil.integration.configuration.ConnectionConfiguration;

public class RedshiftSqlJdbcConnection extends PostgreSqlJdbcConnection {

    public RedshiftSqlJdbcConnection(DTOConnection connectionData) {
        super(connectionData);
    }

    @Override
    protected String getProtocol() {
        return ConnectionConfiguration.REDSHIFT.getProtocol();
    }

    @Override
    protected String getConnectionProperties() {
        return ConnectionConfiguration.REDSHIFT.getProps();
    }

}
