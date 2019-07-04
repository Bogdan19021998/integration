package ai.distil.integration.unit;

import ai.distil.api.internal.model.dto.DTOConnection;
import ai.distil.api.internal.proxy.DataSourceProxy;
import ai.distil.integration.job.sync.AbstractConnection;
import ai.distil.integration.job.sync.holder.DataSourceDataHolder;
import ai.distil.integration.job.sync.jdbc.JdbcConnection;
import ai.distil.integration.job.sync.jdbc.PostgreSqlJdbcConnection;
import ai.distil.integration.job.sync.jdbc.vo.QueryWrapper;
import ai.distil.integration.service.DataSyncService;
import ai.distil.integration.service.sync.ConnectionFactory;
import ai.distil.model.org.ConnectionSettings;
import ai.distil.model.types.ConnectionType;
import com.google.common.collect.Lists;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import ru.yandex.qatools.embed.postgresql.EmbeddedPostgres;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.stream.Stream;

import static ru.yandex.qatools.embed.postgresql.distribution.Version.Main.V9_6;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class PostgreSqlSyncTest extends AbstractSyncTest {

    public static final String DEFAULT_SERVER_ADDRESS = "localhost";
    private static final String DEFAULT_DB_NAME = "postgres";
    private static final String DEFAULT_SCHEMA_NAME = "public";
    @Autowired
    private ConnectionFactory connectionFactory;

    @Autowired
    private DataSyncService dataSyncService;

    @MockBean
    @Autowired
    private DataSourceProxy dataSourceProxy;

    private EmbeddedPostgres postgres;

    @Test
    public void testMySqlConnection() throws Exception {
        DTOConnection connectionDTO = getDefaultConnection();

        try (JdbcConnection jdbcConnection = new PostgreSqlJdbcConnection(connectionDTO);
             QueryWrapper query = jdbcConnection.query("SELECT TABLE_SCHEMA " +
                     "FROM information_schema.tables " +
                     "group by tables.TABLE_SCHEMA")) {

            List<String> schemas = query.readResult(resultSet -> resultSet.getString(1));
//            each postgresql table must have INFORMATION_SCHEMA getSchema
            Assertions.assertTrue(schemas.contains(DEFAULT_SCHEMA_NAME));
        }
    }

    @Test
    public void testSimpleSync() throws Exception {
        DTOConnection connectionDTO = getDefaultConnection();

        long orgId = 15;

        try (AbstractConnection connection = connectionFactory.buildConnection(connectionDTO)) {
            connection.getAllDataSources()
                    .stream()
                    .map(DataSourceDataHolder::mapFromDTODataSourceEntity)
                    .forEach(dataSource -> dataSyncService.reSyncDataSource(orgId, dataSource, connection));
        }
    }

    @Override
    protected void startInstance() throws IOException {
        DTOConnection dtoConnection = getDefaultConnection();
        ConnectionSettings connectionSettings = dtoConnection.getConnectionSettings();

        this.postgres = new EmbeddedPostgres(V9_6);
        String defaultUrl = this.postgres.start(connectionSettings.getServerAddress(), Integer.parseInt(connectionSettings.getPort()),
                connectionSettings.getDatabaseName(), connectionSettings.getUserName(), connectionSettings.getPassword(), Lists.newArrayList());

        String initSql = IOUtils.toString(this.getClass().getClassLoader().getResourceAsStream("seeds/postgresql_seeds.sql"));

        Stream.of(initSql.split(";")).forEach(q -> {
            try (final Connection conn = DriverManager.getConnection(defaultUrl); Statement statement = conn.createStatement()) {
                statement.execute(q);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    protected void stopInstance() {
        this.postgres.stop();
    }

    @Override
    protected void clean() {
//  todo implement
    }

    private DTOConnection getDefaultConnection() {
        DTOConnection connectionDTO = new DTOConnection();
        connectionDTO.setConnectionType(ConnectionType.POSTGRESQL);

        connectionDTO.setConnectionSettings(new ConnectionSettings(
                "username",
                "password",
                null,
                DEFAULT_SERVER_ADDRESS,
                "12345",
                DEFAULT_SCHEMA_NAME,
                DEFAULT_DB_NAME,
                false, null, -1, null, null, null,
                null
        ));
        return connectionDTO;
    }
}
