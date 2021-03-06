package ai.distil.integration;

import ai.distil.api.internal.model.dto.DTOConnection;
import ai.distil.api.internal.model.dto.DTODataSource;
import ai.distil.integration.job.sync.AbstractConnection;
import ai.distil.integration.job.sync.holder.DataSourceDataHolder;
import ai.distil.integration.job.sync.jdbc.RedshiftSqlJdbcConnection;
import ai.distil.integration.job.sync.jdbc.vo.QueryWrapper;
import ai.distil.integration.service.DataSyncService;
import ai.distil.integration.service.RestService;
import ai.distil.integration.service.sync.ConnectionFactory;
import ai.distil.model.org.ConnectionSettings;
import ai.distil.model.types.ConnectionType;
import com.google.common.collect.Sets;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.List;
import java.util.stream.Collectors;

// these tests will work, only if you run the ms sql locally and ingest sample data there, check test/resources/seeds/mssql_seeds.sql
// run development/mssql/docker-compose.yml for set up the sqlserver
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class RedshiftIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private ConnectionFactory connectionFactory;

    @Autowired
    private DataSyncService dataSyncService;

    @MockBean
    @Autowired
    private RestService restService;

    private static final String DEFAULT_DB_NAME = "dwh";
    private static final String DEFAULT_SCHEMA_NAME = "distil_org_crowdcube";


    @Test
    @Disabled
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
    public void syncAllEligibleDataSources() throws Exception {
        try (RedshiftSqlJdbcConnection jdbcConnection = new RedshiftSqlJdbcConnection(getDefaultConnection())) {
            List<DTODataSource> allDataSources = jdbcConnection.getAllDataSources();

            List<DTODataSource> eligibleDataSources = jdbcConnection.getEligibleDataSources();

            eligibleDataSources.stream().filter(v -> Sets.newHashSet("mv_distil_orders")
                    .contains(v.getSourceTableName()))
                    .map(DataSourceDataHolder::mapFromDTODataSourceEntity).forEach(dataSource -> {

                dataSyncService.reSyncDataSource("111", dataSource, jdbcConnection);
                dataSyncService.reSyncDataSource("111", dataSource, jdbcConnection);

                System.out.printf("");
            });


            System.out.println();
        }

    }

    @Test
    @Disabled
    public void testSimpleSync() throws Exception {
        DTOConnection connectionDTO = getDefaultConnection();

        String tenantId = "25";

        try (AbstractConnection connection = connectionFactory.buildConnection(connectionDTO)) {
            List<DTODataSource> allDataSources = connection.getEligibleDataSources();

            boolean dataSourceEligible = connection.isDataSourceEligible(allDataSources.get(0));
            System.out.println();
            List<DataSourceDataHolder> dsHolders = connection.getAllDataSources()
                    .stream()
                    .map(DataSourceDataHolder::mapFromDTODataSourceEntity)
                    .collect(Collectors.toList());

//            SyncProgressTrackingData syncResult = dataSyncService.reSyncDataSource("crowdfund", dsHolders.get(0), connection);
            System.out.println();
//            .forEach(dataSource -> dataSyncService.reSyncDataSource(tenantId, dataSource, connection));
        }
    }

    private DTOConnection getDefaultConnection() {
        DTOConnection connectionDTO = new DTOConnection();
        connectionDTO.setConnectionType(ConnectionType.REDSHIFT);
//        SELECT table_name, table_type  FROM information_schema.tables  WHERE table_schema = ?  ORDER BY table_schema, table_name
        connectionDTO.setConnectionSettings(new ConnectionSettings(
                "distil",
                "c9cfMmBnp[r",
                null,
                null,
                null,
                "redshift.cc-internal.com",
                null,
                null,
                String.valueOf(5439),
                DEFAULT_SCHEMA_NAME,
                DEFAULT_DB_NAME,
                false, null, -1,
                null, null, null,
                null
        ));
        return connectionDTO;
    }
}
