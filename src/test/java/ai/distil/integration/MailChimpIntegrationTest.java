package ai.distil.integration;

import ai.distil.api.internal.model.dto.DTOConnection;
import ai.distil.api.internal.model.dto.DTODataSource;
import ai.distil.api.internal.model.dto.DataSourceHistoryDTO;
import ai.distil.api.internal.model.dto.destination.DestinationDTO;
import ai.distil.api.internal.model.dto.destination.DestinationIntegrationAttributeDTO;
import ai.distil.api.internal.model.dto.destination.DestinationIntegrationDTO;
import ai.distil.api.internal.proxy.ConnectionProxy;
import ai.distil.api.internal.proxy.DataSourceProxy;
import ai.distil.integration.cassandra.repository.CassandraSyncRepository;
import ai.distil.integration.controller.dto.data.DatasetRow;
import ai.distil.integration.job.destination.AbstractDataSync;
import ai.distil.integration.job.destination.vo.CustomAttributeDefinition;
import ai.distil.integration.job.sync.AbstractConnection;
import ai.distil.integration.job.sync.holder.DataSourceDataHolder;
import ai.distil.integration.job.sync.http.AbstractHttpConnection;
import ai.distil.integration.job.sync.http.mailchimp.vo.AudiencesWrapper;
import ai.distil.integration.job.sync.http.mailchimp.vo.MembersWrapper;
import ai.distil.integration.job.sync.http.request.mailchimp.MailChimpAudiencesRequest;
import ai.distil.integration.job.sync.http.request.mailchimp.MailChimpMembersRequest;
import ai.distil.integration.job.sync.http.request.mailchimp.MailChimpMergeFieldsRequest;
import ai.distil.integration.job.sync.http.sync.SyncSettings;
import ai.distil.integration.job.sync.iterator.HttpPaginationRowIterator;
import ai.distil.integration.job.sync.jdbc.SimpleDataSourceDefinition;
import ai.distil.integration.job.sync.progress.SyncProgressTrackingData;
import ai.distil.integration.service.DataSyncService;
import ai.distil.integration.service.RestService;
import ai.distil.integration.service.sync.ConnectionFactory;
import ai.distil.model.org.ConnectionSettings;
import ai.distil.model.types.ConnectionType;
import ai.distil.model.types.DataSourceAttributeType;
import ai.distil.model.types.DataSourceSchemaAttributeTag;
import com.datastax.driver.core.schemabuilder.SchemaBuilder;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static ai.distil.integration.utils.ParseUtils.parseJsonFile;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class MailChimpIntegrationTest {

    public static final String DEFAULT_API_KEY = "a8f9d10cbee86a6377d6a9b5c7c8958e-us3";
    @MockBean
    @Autowired
    private DataSourceProxy dataSourceProxy;

    @MockBean
    @Autowired
    private ConnectionProxy connectionProxy;

    @Autowired
    private ConnectionFactory connectionFactory;

    @Autowired
    private CassandraSyncRepository cassandraSyncRepository;

    @Autowired
    private DataSyncService dataSyncService;

    @SpyBean
    @Autowired
    private RestService restService;


    private static final String DEFAULT_CLIENT_ID = "3e3e1502d7";

    @Test
    public void simpleMailChimpHttpConnectionTest() {
        AbstractHttpConnection connection = (AbstractHttpConnection) connectionFactory.buildConnection(defaultConnection());

        DTODataSource existDataSource = connection.getAllDataSources().stream().filter(v -> DEFAULT_CLIENT_ID.equals(v.getSourceTableName())).findFirst().get();
        Assertions.assertNotNull(existDataSource);

        DataSourceDataHolder dataHolder = DataSourceDataHolder.mapFromDTODataSourceEntity(existDataSource);

        HttpPaginationRowIterator httpPaginationRowIterator = new HttpPaginationRowIterator(connection, dataHolder, new AtomicInteger(0), 2);
        List<DatasetRow> rows = new ArrayList<>();
        httpPaginationRowIterator.forEachRemaining(rows::add);
        Assertions.assertEquals(5, rows.size());
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
    public void getSingleDataSourceTest() {

        DTOConnection dtoConnection = defaultConnection();
        AbstractHttpConnection connection = (AbstractHttpConnection) connectionFactory.buildConnection(dtoConnection);
        DTODataSource existDataSource = connection.getAllDataSources().stream().filter(v -> DEFAULT_CLIENT_ID.equals(v.getSourceTableName())).findFirst().get();

        DTODataSource dataSource = connection.getDataSource(new SimpleDataSourceDefinition(null, DEFAULT_CLIENT_ID, null, null));

        Assertions.assertNotNull(dataSource);
        Assertions.assertTrue(connection.dataSourceExist(DataSourceDataHolder.mapFromDTODataSourceEntity(existDataSource)));

    }

    @Test
    public void reSyncMailChimpWithDynamicFields() throws Exception {
        String tenantId = "35";

        cassandraSyncRepository.getConnection().getSession().execute(SchemaBuilder.dropKeyspace(String.format("distil_org_%s", tenantId)).ifExists());
        DTOConnection connectionDTO = defaultConnection();

        Mockito.doReturn(parseJsonFile("mocks/mailchimp/mailchimps_lists.json", new TypeReference<AudiencesWrapper>() {
        }))
                .when(restService).execute(Mockito.any(), Mockito.any(MailChimpAudiencesRequest.class), Mockito.any());

        Mockito.doReturn(parseJsonFile("mocks/mailchimp/mailchimps_members.json", new TypeReference<MembersWrapper>() {
        }))
                .when(restService).execute(Mockito.any(), Mockito.any(MailChimpMembersRequest.class), Mockito.any());

        Mockito.doReturn(parseJsonFile("mocks/mailchimp/mailchimps_merge_fields.json", new TypeReference<Map<String, Object>>() {
        }))
                .when(restService).execute(Mockito.any(), Mockito.any(MailChimpMergeFieldsRequest.class), Mockito.any());

        DataSourceDataHolder oldDataSource;

        try (AbstractConnection connection = connectionFactory.buildConnection(connectionDTO)) {
            oldDataSource = DataSourceDataHolder.mapFromDTODataSourceEntity(connection.getAllDataSources().get(0));
            SyncProgressTrackingData syncResults = dataSyncService.reSyncDataSource(tenantId, oldDataSource, connection);
//            check we processed 5 rows
            Assertions.assertEquals(5, syncResults.getProcessed());
//            retrieve all existing rows
            List<Map<String, Object>> allRows = cassandraSyncRepository.selectAllToMap(tenantId, oldDataSource);
//            check that existing rows count match processed ones
            Assertions.assertEquals(allRows.size(), syncResults.getProcessed());
//            check that columns count in table is correct
            Assertions.assertEquals(allRows.get(0).keySet().size(), 52);

        }

        Mockito.doReturn(parseJsonFile("mocks/mailchimp/new_mailchimps_members.json", new TypeReference<MembersWrapper>() {
        }))
                .when(restService).execute(Mockito.any(), Mockito.any(MailChimpMembersRequest.class), Mockito.any());

        Mockito.doReturn(parseJsonFile("mocks/mailchimp/new_mailchimps_merge_fields.json", new TypeReference<Map<String, Object>>() {
        }))
                .when(restService).execute(Mockito.any(), Mockito.any(MailChimpMergeFieldsRequest.class), Mockito.any());
        try (AbstractConnection connection = connectionFactory.buildConnection(connectionDTO)) {
            SyncProgressTrackingData syncResults = dataSyncService.reSyncDataSource(tenantId, oldDataSource, connection);
            DataSourceDataHolder newDataSource = DataSourceDataHolder.mapFromDTODataSourceEntity(connection.getAllDataSources().get(0));
//            check we processed 5 rows
            Assertions.assertEquals(5, syncResults.getProcessed());
//            check we updated all 5 rows
            Assertions.assertEquals(5, syncResults.getUpdated());

            List<Map<String, Object>> allRows = cassandraSyncRepository.selectAllToMap(tenantId, newDataSource);
//            check that existing rows count match processed ones
            Assertions.assertEquals(allRows.size(), syncResults.getProcessed());
            //            check that columns count in table is correct
            Assertions.assertEquals(allRows.get(0).keySet().size(), 47);

        }
    }


    @Test
    public void syncWithDuplicates() throws Exception {
        String tenantId = "35";

        cassandraSyncRepository.getConnection().getSession().execute(SchemaBuilder.dropKeyspace(String.format("distil_org_%s", tenantId)).ifExists());
        DTOConnection connectionDTO = defaultConnection();

        Mockito.doReturn(parseJsonFile("mocks/mailchimp/mailchimps_lists.json", new TypeReference<AudiencesWrapper>() {
        }))
                .when(restService).execute(Mockito.any(), Mockito.any(MailChimpAudiencesRequest.class), Mockito.any());

        Mockito.doReturn(parseJsonFile("mocks/mailchimp/mailchimps_members_duplicates.json", new TypeReference<MembersWrapper>() {
        }))
                .when(restService).execute(Mockito.any(), Mockito.any(MailChimpMembersRequest.class), Mockito.any());

        Mockito.doReturn(parseJsonFile("mocks/mailchimp/mailchimps_merge_fields.json", new TypeReference<Map<String, Object>>() {
        }))
                .when(restService).execute(Mockito.any(), Mockito.any(MailChimpMergeFieldsRequest.class), Mockito.any());

        DataSourceDataHolder oldDataSource;

        try (AbstractConnection connection = connectionFactory.buildConnection(connectionDTO)) {
            oldDataSource = DataSourceDataHolder.mapFromDTODataSourceEntity(connection.getAllDataSources().get(0));
            ArgumentCaptor<String> tenantIdArgumentCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<DataSourceHistoryDTO> dataSourceHistoryArgumentCaptor = ArgumentCaptor.forClass(DataSourceHistoryDTO.class);
            Mockito.when(dataSourceProxy.save(tenantIdArgumentCaptor.capture(), dataSourceHistoryArgumentCaptor.capture()))
                    .thenReturn(null);

            SyncProgressTrackingData syncResults = dataSyncService.reSyncDataSource(tenantId, oldDataSource, connection);
//            check we processed 5 rows
            Assertions.assertEquals(3, syncResults.getProcessed());
//            retrieve all existing rows
            List<Map<String, Object>> allRows = cassandraSyncRepository.selectAllToMap(tenantId, oldDataSource);
//            check that existing rows count match processed ones
            Assertions.assertEquals(allRows.size(), syncResults.getCurrentRowsCount());
            Assertions.assertEquals(2, syncResults.getDuplicates());

            Assertions.assertTrue(dataSourceHistoryArgumentCaptor.getValue().getEncounteredError());

        }

    }

    @Test
    public void testSimpleSync() throws Exception {
        DTOConnection connectionDTO = defaultConnection();
        String tenantId = "30";

        cassandraSyncRepository.getConnection().getSession().execute(SchemaBuilder.dropKeyspace(String.format("distil_org_%s", tenantId)).ifExists());

        try (AbstractConnection connection = connectionFactory.buildConnection(connectionDTO)) {
            connection.getAllDataSources()
                    .stream()
                    .map(DataSourceDataHolder::mapFromDTODataSourceEntity)
                    .forEach(dataSource -> dataSyncService.reSyncDataSource(tenantId, dataSource, connection));
        }


    }

    @Test
    public void testSimpleIngestion() throws Exception {
        AbstractDataSync dataSync = connectionFactory.buildDataSync(new DestinationDTO(), defaultConnection(), defaultDestination(), new SyncSettings(5), Collections.emptyList());
        String listId = dataSync.createListIfNotExists();
        List<CustomAttributeDefinition> customAttributeDefinitions = dataSync.syncCustomAttributesSchema(listId);

        dataSync.ingestData(listId, customAttributeDefinitions, null);

        System.out.println();
    }

    private DestinationIntegrationDTO defaultDestination() {
        DestinationIntegrationDTO integration = new DestinationIntegrationDTO();

        long id = 1L;
        integration.setId(id);
        integration.setDestinationId(id);

        integration.setAttributes(Lists.newArrayList(
                new DestinationIntegrationAttributeDTO(1L, DataSourceSchemaAttributeTag.CUSTOMER_EXTERNAL_ID, "test1", "test1", DataSourceAttributeType.STRING, true, true),
                new DestinationIntegrationAttributeDTO(2L, DataSourceSchemaAttributeTag.CUSTOMER_COUNTRY_CODE, "test2", "test2", DataSourceAttributeType.STRING, true, true),
                new DestinationIntegrationAttributeDTO(3L, DataSourceSchemaAttributeTag.CUSTOMER_EMAIL_ADDRESS, "test3", "test3", DataSourceAttributeType.STRING, true, true)
        ));

        return integration;
    }

    private DTOConnection defaultConnection() {
        DTOConnection dtoConnection = new DTOConnection();
        dtoConnection.setConnectionType(ConnectionType.MAILCHIMP);
        ConnectionSettings connectionSettings = new ConnectionSettings();
        connectionSettings.setApiKey(DEFAULT_API_KEY);
        dtoConnection.setConnectionSettings(connectionSettings);

        return dtoConnection;
    }


}
