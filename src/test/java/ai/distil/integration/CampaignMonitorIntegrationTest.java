package ai.distil.integration;

import ai.distil.api.internal.model.dto.DTOConnection;
import ai.distil.api.internal.model.dto.DTODataSource;
import ai.distil.api.internal.model.dto.destination.DestinationDTO;
import ai.distil.api.internal.model.dto.destination.DestinationIntegrationAttributeDTO;
import ai.distil.api.internal.model.dto.destination.DestinationIntegrationDTO;
import ai.distil.api.internal.proxy.DataSourceProxy;
import ai.distil.integration.cassandra.repository.CassandraSyncRepository;
import ai.distil.integration.controller.dto.data.DatasetRow;
import ai.distil.integration.job.destination.AbstractDataSync;
import ai.distil.integration.job.destination.vo.CustomAttributeDefinition;
import ai.distil.integration.job.sync.AbstractConnection;
import ai.distil.integration.job.sync.holder.DataSourceDataHolder;
import ai.distil.integration.job.sync.http.AbstractHttpConnection;
import ai.distil.integration.job.sync.http.sync.SyncSettings;
import ai.distil.integration.job.sync.iterator.HttpPaginationRowIterator;
import ai.distil.integration.service.DataSyncService;
import ai.distil.integration.service.RestService;
import ai.distil.integration.service.sync.ConnectionFactory;
import ai.distil.model.org.ConnectionSettings;
import ai.distil.model.types.ConnectionType;
import ai.distil.model.types.DataSourceAttributeType;
import ai.distil.model.types.DataSourceSchemaAttributeTag;
import com.datastax.driver.core.schemabuilder.SchemaBuilder;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class CampaignMonitorIntegrationTest {

    public static final String DEFAULT_API_KEY = "eEJNZDdVY2s3QVU0MDJrOXkvVFAyTFRjTjdpdmQ2Q1RIK0ZYZWFXUWJhVEtudTN3Tm5QdVlENi9sbzFzOTdsaWNZb0M3MnNsd0cvdDJzUnpldm9FTURLUGFpcFdXY0FPYzZKQzJncDJFc0RRSzExL2NSS05ka2ZUVGg0VU51cmdJZ1RGOUhnbXQ3TDZkQi9kcDMyMnR3PT06";

    @MockBean
    @Autowired
    private DataSourceProxy dataSourceProxy;

    @Autowired
    private ConnectionFactory connectionFactory;

    @Autowired
    private CassandraSyncRepository cassandraSyncRepository;

    @Autowired
    private DataSyncService dataSyncService;

    @SpyBean
    @Autowired
    private RestService restService;

    private static final String DEFAULT_LIST_ID = "00ba8210dc05fdf37464eb8eca7f3d48";

    @Test
    public void simpleCampaignMonitorTest() {
        AbstractHttpConnection connection = (AbstractHttpConnection) connectionFactory.buildConnection(defaultConnection());

        DTODataSource existDataSource = connection.getAllDataSources().stream().filter(v -> DEFAULT_LIST_ID.equals(v.getSourceTableName())).findFirst().get();
        Assertions.assertNotNull(existDataSource);

        DataSourceDataHolder dataHolder = DataSourceDataHolder.mapFromDTODataSourceEntity(existDataSource);

        HttpPaginationRowIterator httpPaginationRowIterator = new HttpPaginationRowIterator(connection, dataHolder, new AtomicInteger(1), 10);
        List<DatasetRow> rows = new ArrayList<>(Lists.newArrayList(httpPaginationRowIterator));
        Assertions.assertEquals(rows.size(), 5);
    }

    @Test
    public void checkSourceAvailabilityTest() {
        DTOConnection dtoConnection = defaultConnection();
        AbstractHttpConnection connection = (AbstractHttpConnection) connectionFactory.buildConnection(dtoConnection);
        Assertions.assertTrue(connection.isAvailable());

        dtoConnection.getConnectionSettings().setApiKey("somefakeapikey");
        Assertions.assertFalse(connection.isAvailable());
    }

    @Test
    public void testSimpleSync() throws Exception {
        DTOConnection connectionDTO = defaultConnection();
        String tenantId = "130";

        cassandraSyncRepository.getConnection().getSession().execute(SchemaBuilder.dropKeyspace(String.format("distil_org_%s", tenantId)).ifExists());

        try (AbstractConnection connection = connectionFactory.buildConnection(connectionDTO)) {
            connection.getAllDataSources()
                    .stream()
                    .map(DataSourceDataHolder::mapFromDTODataSourceEntity)
                    .forEach(dataSource -> dataSyncService.reSyncDataSource(tenantId, dataSource, connection));
        }

    }

    @Test
    public void createCampaignMonitor() {
        DTOConnection connectionDTO = defaultConnection();
        AbstractDataSync dataSync = connectionFactory.buildDataSync(new DestinationDTO(), connectionDTO, defaultDestination(), new SyncSettings(5), Lists.newArrayList());
        String listId = dataSync.createListIfNotExists();
        List<CustomAttributeDefinition> attributes = dataSync.syncCustomAttributesSchema(listId);

        dataSync.ingestData(listId, attributes, null);
    }

    private DestinationIntegrationDTO defaultDestination() {
        DestinationIntegrationDTO integration = new DestinationIntegrationDTO();

        long id = 1L;
        integration.setId(id);
        integration.setDestinationId(1L);

        integration.setAttributes(Lists.newArrayList(
                new DestinationIntegrationAttributeDTO(1L, DataSourceSchemaAttributeTag.CUSTOMER_EXTERNAL_ID, "test1", "test1", DataSourceAttributeType.STRING, true, true),
                new DestinationIntegrationAttributeDTO(2L, DataSourceSchemaAttributeTag.CUSTOMER_COUNTRY_CODE, "test2", "test2", DataSourceAttributeType.STRING, true, true),
                new DestinationIntegrationAttributeDTO(3L, DataSourceSchemaAttributeTag.CUSTOMER_EMAIL_ADDRESS, "test3", "test3", DataSourceAttributeType.STRING, true, true)
        ));

        return integration;
    }

    private DTOConnection defaultConnection() {
        DTOConnection dtoConnection = new DTOConnection();
        dtoConnection.setConnectionType(ConnectionType.CAMPAIGN_MONITOR);
        ConnectionSettings connectionSettings = new ConnectionSettings();
        connectionSettings.setApiKey(DEFAULT_API_KEY);
        dtoConnection.setConnectionSettings(connectionSettings);

        return dtoConnection;
    }

}
