package ai.distil.integration;

import ai.distil.api.internal.model.dto.DTOConnection;
import ai.distil.api.internal.proxy.DataSourceProxy;
import ai.distil.integration.job.sync.AbstractConnection;
import ai.distil.integration.job.sync.SyncTableDefinition;
import ai.distil.integration.job.sync.holder.DataSourceDataHolder;
import ai.distil.integration.job.sync.jdbc.RedshiftSqlJdbcConnection;
import ai.distil.integration.job.sync.jdbc.vo.QueryWrapper;
import ai.distil.integration.service.DataSyncService;
import ai.distil.integration.service.sync.ConnectionFactory;
import ai.distil.model.org.ConnectionSettings;
import ai.distil.model.types.ConnectionType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.List;

// these tests will work, only if you run the ms sql locally and ingest sample data there, check test/resources/seeds/mssql_seeds.sql
// run development/mssql/docker-compose.yml for set up the sqlserver
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class RedshiftIntegrationTest {

    @Autowired
    private ConnectionFactory connectionFactory;

    @Autowired
    private DataSyncService dataSyncService;

    @MockBean
    @Autowired
    private DataSourceProxy dataSourceProxy;

    private static final String DEFAULT_DB_NAME = "distil_test";
    private static final String DEFAULT_SCHEMA_NAME = "sync";


    @Test
    public void simpleConnectionTest() throws Exception {

        try (RedshiftSqlJdbcConnection jdbcConnection = new RedshiftSqlJdbcConnection(getDefaultConnection())) {
            QueryWrapper query = jdbcConnection.query("SELECT TABLE_SCHEMA " +
                    "FROM information_schema.tables " +
                    "group by tables.TABLE_SCHEMA");

            List<String> schemas = query.readResult(resultSet -> resultSet.getString(1));
            Assertions.assertTrue(schemas.contains(DEFAULT_SCHEMA_NAME));
        }
    }

    @Test
    @Disabled
    public void testSimpleSync() throws Exception {
        DTOConnection connectionDTO = getDefaultConnection();

        long orgId = 25;

        try (AbstractConnection connection = connectionFactory.buildConnection(connectionDTO)) {
            connection.getAllDataSources()
                    .stream()
                    .filter(dataSource -> SyncTableDefinition.isTableEligibleForRun(dataSource.getSourceTableName()))
                    .map(DataSourceDataHolder::mapFromDTODataSourceEntity)
                    .forEach(dataSource -> dataSyncService.reSyncDataSource(orgId, dataSource, connection));
        }
    }

    private DTOConnection getDefaultConnection() {
        DTOConnection connectionDTO = new DTOConnection();
        connectionDTO.setConnectionType(ConnectionType.REDSHIFT);
//        SELECT table_name, table_type  FROM information_schema.tables  WHERE table_schema = ?  ORDER BY table_schema, table_name
        connectionDTO.setConnectionSettings(new ConnectionSettings(
                "vitaliy",
                "JtL4A7+Waq",
                null,
                "dwh.clgb5kxf1w5l.eu-west-2.redshift.amazonaws.com",
                String.valueOf(5439),
                DEFAULT_SCHEMA_NAME,
                DEFAULT_DB_NAME,
                false, null, -1, null, null, null,
                null
        ));
        return connectionDTO;
    }
}
