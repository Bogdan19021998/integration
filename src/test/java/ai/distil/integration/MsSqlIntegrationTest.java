package ai.distil.integration;

import ai.distil.api.internal.model.dto.DTOConnection;
import ai.distil.api.internal.proxy.DataSourceProxy;
import ai.distil.integration.job.sync.AbstractConnection;
import ai.distil.integration.job.sync.holder.DataSourceDataHolder;
import ai.distil.integration.job.sync.jdbc.MsSqlJdbcConnection;
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
public class MsSqlIntegrationTest {

    @Autowired
    private ConnectionFactory connectionFactory;

    @Autowired
    private DataSyncService dataSyncService;

    @MockBean
    @Autowired
    private DataSourceProxy dataSourceProxy;

    private static final String DEFAULT_SCHEMA_NAME = "distil";

    @Test
    @Disabled
    public void simpleConnectionTest() throws Exception {

        try (MsSqlJdbcConnection jdbcConnection = new MsSqlJdbcConnection(getDefaultConnection())) {
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

        long orgId = 20;

        try (AbstractConnection connection = connectionFactory.buildConnection(connectionDTO)) {
            connection.getAllDataSources()
                    .stream()
                    .map(DataSourceDataHolder::mapFromDTODataSourceEntity)
                    .forEach(dataSource -> dataSyncService.reSyncDataSource(orgId, dataSource, connection));
        }
    }

    private DTOConnection getDefaultConnection() {
        DTOConnection connectionDTO = new DTOConnection();
        connectionDTO.setConnectionType(ConnectionType.SQLSERVER);
//        SELECT table_name, table_type  FROM information_schema.tables  WHERE table_schema = ?  ORDER BY table_schema, table_name
        connectionDTO.setConnectionSettings(new ConnectionSettings(
                "SA",
                "Distli123",
                null,
                "localhost",
                String.valueOf(1433),
                "master",
                "distil",
                false, null, -1, null, null, null,
                null
        ));
        return connectionDTO;
    }
}
