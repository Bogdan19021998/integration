package ai.distil.integration.unit;

import ai.distil.api.internal.model.dto.DTOConnection;
import ai.distil.api.internal.model.dto.DTODataSource;
import ai.distil.api.internal.proxy.DataSourceProxy;
import ai.distil.integration.cassandra.repository.CassandraSyncRepository;
import ai.distil.integration.job.sync.AbstractConnection;
import ai.distil.integration.job.sync.holder.DataSourceDataHolder;
import ai.distil.integration.job.sync.jdbc.JdbcConnection;
import ai.distil.integration.job.sync.jdbc.MySqlJdbcConnection;
import ai.distil.integration.job.sync.jdbc.vo.QueryWrapper;
import ai.distil.integration.job.sync.progress.SyncProgressTrackingData;
import ai.distil.integration.service.DataSyncService;
import ai.distil.integration.service.sync.ConnectionFactory;
import ai.distil.integration.utils.ColumnsUtils;
import ai.distil.model.org.ConnectionSettings;
import ai.distil.model.types.ConnectionType;
import ai.distil.model.types.DataSourceType;
import com.datastax.driver.core.ColumnDefinitions;
import com.datastax.driver.core.schemabuilder.SchemaBuilder;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Sets;
import com.wix.mysql.EmbeddedMysql;
import com.wix.mysql.ScriptResolver;
import com.wix.mysql.config.MysqldConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.wix.mysql.EmbeddedMysql.anEmbeddedMysql;
import static com.wix.mysql.config.Charset.UTF8;
import static com.wix.mysql.config.MysqldConfig.aMysqldConfig;
import static com.wix.mysql.distribution.Version.v5_6_24;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class MySqlSyncTest extends AbstractSyncTest {


    private static final String CUSTOMERS_TABLE_NAME = "distil_customers";
    private static final String CONTENT_TABLE_NAME = "distil_content";
    private static final String DEFAULT_SERVER_ADDRESS = "localhost";
    private static final String DEFAULT_SCHEMA_NAME = "distil";
    private static final String MYSQL_SEEDS_FILE = "seeds/mysql_seeds_v2.sql";
    private static final String MYSQL_SYNC_RESULTS_FILE = "seeds/expected_results/mysql_sync_results.json";

    public static final Set<String> DYNAMIC_FIELDS_TO_AVOID = Sets.newHashSet("u", "c");

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ConnectionFactory connectionFactory;

    @Autowired
    private DataSyncService dataSyncService;

    @Autowired
    private CassandraSyncRepository cassandraSyncRepository;

    @MockBean
    @Autowired
    private DataSourceProxy dataSourceProxy;

    private EmbeddedMysql mysqld;
    private MysqldConfig config;

    @Test
    public void testMySqlConnection() throws Exception {
        DTOConnection connectionDTO = getDefaultConnection();

        try (JdbcConnection jdbcConnection = new MySqlJdbcConnection(connectionDTO);
             QueryWrapper query = jdbcConnection.query("SELECT TABLE_SCHEMA " +
                     "FROM information_schema.tables " +
                     "group by tables.TABLE_SCHEMA")) {

            List<String> schemas = query.readResult(resultSet -> resultSet.getString(1));
//            each mysql table must have INFORMATION_SCHEMA getSchema
            Assertions.assertTrue(schemas.contains(DEFAULT_SCHEMA_NAME));
        }
    }

    @Test
    public void testSimpleSync() throws Exception {
        DTOConnection connectionDTO = getDefaultConnection();

        String tenantId = "5";
        cassandraSyncRepository.getConnection().getSession().execute(SchemaBuilder.dropKeyspace(CassandraSyncRepository.KEYSPACE_PREFIX + tenantId).ifExists());

        List<Map<String, Object>> expectedResult = ((List<Map<String, Object>>) objectMapper.readValue(this.getClass().getClassLoader().getResourceAsStream(MYSQL_SYNC_RESULTS_FILE),
                new TypeReference<List<TreeMap<String, Object>>>() {
                })).stream().map(r -> {
            Map<String, Object> result = new TreeMap<>();
            r.keySet().removeAll(DYNAMIC_FIELDS_TO_AVOID);

            r.forEach((s, o) -> result.put(ColumnsUtils.normalizeKeyForComparison(s), String.valueOf(o)));
            return result;
        }).collect(Collectors.toList());

        try (AbstractConnection connection = connectionFactory.buildConnection(connectionDTO)) {
            connection.getAllDataSources()
                    .stream()
                    .filter(d -> CUSTOMERS_TABLE_NAME.equals(d.getSourceTableName()))
                    .map(DataSourceDataHolder::mapFromDTODataSourceEntity)
                    .forEach(dataSource -> {
                        SyncProgressTrackingData syncTrackingData = dataSyncService.reSyncDataSource(tenantId, dataSource, connection);
                        Assertions.assertEquals(expectedResult.size(), syncTrackingData.getCurrentRowsCount());

                        List<Map<String, Object>> syncResult = cassandraSyncRepository.selectAllToMap(tenantId, dataSource).stream().map(m -> {
                            Map<String, Object> result = new TreeMap<>();
                            m.forEach((s, o) -> result.put(ColumnsUtils.normalizeKeyForComparison(s), o));
                            return result;
                        }).collect(Collectors.toList());

                        syncResult.stream().peek(row -> {
                            row.keySet().removeAll(DYNAMIC_FIELDS_TO_AVOID);
                            row.forEach((s, o) -> row.put(s, String.valueOf(o)));
                        }).forEach(row -> {
                            Assertions.assertTrue(expectedResult.contains(row));
                        });
                    });
        }

    }

    @Test
    public void mandatoryFieldsSync() throws Exception {
        DTOConnection connectionDTO = getDefaultConnection();
        String tenantId = "1";
        cassandraSyncRepository.getConnection().getSession().execute(SchemaBuilder.dropKeyspace(CassandraSyncRepository.KEYSPACE_PREFIX + tenantId).ifExists());


        try (JdbcConnection connection = (JdbcConnection) connectionFactory.buildConnection(connectionDTO)) {
            List<DTODataSource> allDataSources = connection.getEligibleDataSources();
            Assertions.assertEquals(allDataSources.size(), 1);

            connection.execute(String.format("alter table %s add column URL text", CONTENT_TABLE_NAME));

            allDataSources = connection.getEligibleDataSources();
            Assertions.assertEquals(allDataSources.size(), 2);
        }
    }

    @Test
    public void reSyncDataTest() throws Exception {
        DTOConnection connectionDTO = getDefaultConnection();

        String tenantId = "4";
        cassandraSyncRepository.getConnection().getSession().execute(SchemaBuilder.dropKeyspace(CassandraSyncRepository.KEYSPACE_PREFIX + tenantId).ifExists());
        // do the basic sync
        try (JdbcConnection connection = (JdbcConnection) connectionFactory.buildConnection(connectionDTO)) {
            DTODataSource dtoDataSource = connection.getAllDataSources().stream().filter(d -> d.getSourceTableName().equals(CUSTOMERS_TABLE_NAME))
                    .findFirst()
                    .orElse(null);
            DataSourceDataHolder dataSourceDataHolder = DataSourceDataHolder.mapFromDTODataSourceEntity(dtoDataSource);
            cassandraSyncRepository.dropTableIfExists(tenantId, dataSourceDataHolder);

            dataSyncService.reSyncDataSource(tenantId, dataSourceDataHolder, connection);

            Set<String> expectedColumns = Sets.newHashSet("ctimestamp_field_1145404271", "ctext_field_2112752792",
                    "ctime_field_1123633416", "cdec_field_59113085", "cblob_field_61622744", "cint_field_319859882",
                    "ctinyint_field_1085818454", "creal_field_1659752679", "cfixed_field_1392739439", "cnumeric_field_2053119672",
                    "cyear_field_2005292424", "cchar_field_1190608593", "cfloat_field_1470697129", "cenum_field_1103293380",
                    "c", "cmediumint_field_724324139", "cid_3355", "cdouble_precision_field_351236491", "h", "cinteger_field_729932103",
                    "cnull_field_2032121790", "csmallint_field_515715197", "p", "u", "cbit_field_36977848", "cdouble_field_1376821004",
                    "cdatetime_field_402323254", "cdate_field_862386473", "cbinary_field_340675388", "cdecimal_field_1901500044")
                    .stream()
                    .map(ColumnsUtils::normalizeKeyForComparison)
                    .collect(Collectors.toSet());

            AtomicInteger rowsCounter = new AtomicInteger();
            Set<String> actualColumns = new HashSet<>();

            cassandraSyncRepository.selectAll(tenantId, dataSourceDataHolder).forEach(row -> {
                ColumnDefinitions columnDefinitions = row.getColumnDefinitions();

                rowsCounter.incrementAndGet();
                actualColumns.addAll(columnDefinitions.asList()
                        .stream()
                        .map(cd -> ColumnsUtils.normalizeKeyForComparison(cd.getName()))
                        .collect(Collectors.toSet()));
            });

//          check that rows count and columns are as expected
            Assertions.assertEquals(3, rowsCounter.get());
            Assertions.assertEquals(expectedColumns, actualColumns);


            connection.execute(String.format("ALTER TABLE %s ADD COLUMN test_new_1 int", CUSTOMERS_TABLE_NAME));
            connection.execute(String.format("ALTER TABLE %s ADD COLUMN test_new_2 int", CUSTOMERS_TABLE_NAME));
            connection.execute(String.format("ALTER TABLE %s DROP COLUMN timestamp_field", CUSTOMERS_TABLE_NAME));
            connection.execute(String.format("ALTER TABLE %s MODIFY COLUMN date_field varchar(24)", CUSTOMERS_TABLE_NAME));
//          insert one more row
            connection.execute(String.format(
                    "insert into %s " +
                            " values ('4', 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, " +
                            " '2019-01-01', '08:00:00', '9999-12-31 23:59:59', '1970', '0', '0', '0', '0', 'M', '0', 0, 0) ",
                    CUSTOMERS_TABLE_NAME));

            dataSyncService.reSyncDataSource(tenantId, dataSourceDataHolder, connection);
//          removed gender, added test_new_1 and test_new_2
            Set<String> expectedNewColumns = new HashSet<>(expectedColumns);
            expectedNewColumns.remove("ctimestamp_field");
            expectedNewColumns.add("ctest_new_2");
            expectedNewColumns.add("ctest_new_1");

            int expectedNewRowsCount = 4;

            AtomicInteger newRowsCounter = new AtomicInteger();
            Set<String> newActualColumns = new HashSet<>();

            cassandraSyncRepository.selectAll(tenantId, dataSourceDataHolder).forEach(row -> {
                ColumnDefinitions columnDefinitions = row.getColumnDefinitions();

                newRowsCounter.incrementAndGet();
                newActualColumns.addAll(columnDefinitions.asList()
                        .stream()
                        .map(cd -> ColumnsUtils.normalizeKeyForComparison(cd.getName()))
                        .collect(Collectors.toSet()));
            });

            Assertions.assertEquals(expectedNewRowsCount, newRowsCounter.get());
            Assertions.assertEquals(expectedNewColumns, newActualColumns);

        }

    }

    @Test
    public void tableExistingCheckTest() throws Exception {
        DTOConnection connectionDTO = getDefaultConnection();

        // do the basic sync
        try (JdbcConnection connection = (JdbcConnection) connectionFactory.buildConnection(connectionDTO)) {

            DataSourceDataHolder holder = new DataSourceDataHolder(CUSTOMERS_TABLE_NAME, CUSTOMERS_TABLE_NAME, Collections.emptyList(), DataSourceType.CUSTOMER, 1L);
            boolean isTableExists = connection.dataSourceExist(holder);
            Assertions.assertTrue(isTableExists, "Consumer table must exists");

            connection.execute("DROP TABLE " + holder.getDataSourceId());

            boolean isTableExistsAfterRemoving = connection.dataSourceExist(holder);
            Assertions.assertFalse(isTableExistsAfterRemoving, "Consumer table must be removed");
        }
    }

    @Test
    public void syncWithDuplicatesTest() throws Exception {
        DTOConnection connectionDTO = getDefaultConnection();

        // do the basic sync
        try (JdbcConnection connection = (JdbcConnection) connectionFactory.buildConnection(connectionDTO)) {

            DataSourceDataHolder holder = new DataSourceDataHolder(CUSTOMERS_TABLE_NAME, CUSTOMERS_TABLE_NAME, Collections.emptyList(), DataSourceType.CUSTOMER, 1L);
            boolean isTableExists = connection.dataSourceExist(holder);
            Assertions.assertTrue(isTableExists, "Consumer table must exists");

            connection.execute("DROP TABLE " + holder.getDataSourceId());

            boolean isTableExistsAfterRemoving = connection.dataSourceExist(holder);
            Assertions.assertFalse(isTableExistsAfterRemoving, "Consumer table must be removed");
        }
    }

    @Override
    protected void startInstance() throws IOException {

        this.config = aMysqldConfig(v5_6_24)
                .withCharset(UTF8)
                .withFreePort()
                .withUser("differentUser", "anotherPassword")
                .withTimeZone("Europe/Vilnius")
                .withTimeout(2, TimeUnit.MINUTES)
                .withServerVariable("max_connect_errors", 666)
                .build();

        this.mysqld = anEmbeddedMysql(this.config)
                .addSchema(DEFAULT_SCHEMA_NAME, ScriptResolver.classPathScript(MYSQL_SEEDS_FILE))
                .start();
    }

    @Override
    protected void stopInstance() {
        mysqld.stop();
    }

    @Override
    protected void clean() {
        this.mysqld.reloadSchema(DEFAULT_SCHEMA_NAME, ScriptResolver.classPathScript(MYSQL_SEEDS_FILE));
    }

    private DTOConnection getDefaultConnection() {
        DTOConnection connectionDTO = new DTOConnection();
        connectionDTO.setConnectionType(ConnectionType.MYSQL);

        connectionDTO.setConnectionSettings(new ConnectionSettings(
                this.config.getUsername(),
                this.config.getPassword(),
                null,
                null,
                null,
                DEFAULT_SERVER_ADDRESS,
                String.valueOf(this.config.getPort()),
                DEFAULT_SCHEMA_NAME,
                null,
                false, null, -1, null, null, null,
                null
        ));
        return connectionDTO;
    }
}
