package ai.distil.integration;

import ai.distil.api.internal.model.dto.DTOConnection;
import ai.distil.api.internal.model.dto.DTODataSource;
import ai.distil.api.internal.proxy.DataSourceProxy;
import ai.distil.integration.cassandra.repository.CassandraSyncRepository;
import ai.distil.integration.controller.dto.data.DatasetRow;
import ai.distil.integration.job.sync.AbstractConnection;
import ai.distil.integration.job.sync.holder.DataSourceDataHolder;
import ai.distil.integration.job.sync.http.AbstractHttpConnection;
import ai.distil.integration.job.sync.http.JsonDataConverter;
import ai.distil.integration.job.sync.http.mailchimp.vo.AudiencesWrapper;
import ai.distil.integration.job.sync.http.mailchimp.vo.MembersWrapper;
import ai.distil.integration.job.sync.http.request.mailchimp.MailChimpAudiencesRequest;
import ai.distil.integration.job.sync.http.request.mailchimp.MailChimpMembersRequest;
import ai.distil.integration.job.sync.http.request.mailchimp.MailChimpMergeFieldsRequest;
import ai.distil.integration.job.sync.iterator.HttpPaginationRowIterator;
import ai.distil.integration.job.sync.jdbc.SimpleDataSourceDefinition;
import ai.distil.integration.job.sync.progress.SyncProgressTrackingData;
import ai.distil.integration.service.DataSyncService;
import ai.distil.integration.service.RestService;
import ai.distil.integration.service.sync.ConnectionFactory;
import ai.distil.model.org.ConnectionSettings;
import ai.distil.model.types.ConnectionType;
import com.datastax.driver.core.schemabuilder.SchemaBuilder;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class MailChimpIntegrationTest {

    public static final String DEFAULT_API_KEY = "YXBpOmE4ZjlkMTBjYmVlODZhNjM3N2Q2YTliNWM3Yzg5NThlLXVzMw==";
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


    private static final String DEFAULT_CLIENT_ID = "3e3e1502d7";

    @Test
    public void simpleMailChimpHttpConnectionTest() {
        AbstractHttpConnection connection = (AbstractHttpConnection) connectionFactory.buildConnection(defaultConnection());

        DTODataSource existDataSource = connection.getAllDataSources().stream().filter(v -> DEFAULT_CLIENT_ID.equals(v.getSourceTableName())).findFirst().get();
        Assertions.assertNotNull(existDataSource);

        DataSourceDataHolder dataHolder = DataSourceDataHolder.mapFromDTODataSourceEntity(existDataSource);

        HttpPaginationRowIterator httpPaginationRowIterator = new HttpPaginationRowIterator(connection, dataHolder, new AtomicInteger(0), 2);
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
        long orgId = 35;

        cassandraSyncRepository.getConnection().getSession().execute(SchemaBuilder.dropKeyspace(String.format("org_%s", orgId)));
        DTOConnection connectionDTO = defaultConnection();

        Mockito.doReturn(parseJsonFile("mocks/mailchimp/mailchimps_lists.json", new TypeReference<AudiencesWrapper>() {}))
                .when(restService).execute(Mockito.any(), Mockito.any(MailChimpAudiencesRequest.class), Mockito.any());

        Mockito.doReturn(parseJsonFile("mocks/mailchimp/mailchimps_members.json", new TypeReference<MembersWrapper>() {}))
                .when(restService).execute(Mockito.any(), Mockito.any(MailChimpMembersRequest.class), Mockito.any());

        Mockito.doReturn(parseJsonFile("mocks/mailchimp/mailchimps_merge_fields.json", new TypeReference<Map<String, Object>>() {}))
                .when(restService).execute(Mockito.any(), Mockito.any(MailChimpMergeFieldsRequest.class), Mockito.any());

        DataSourceDataHolder oldDataSource;

        try (AbstractConnection connection = connectionFactory.buildConnection(connectionDTO)) {
            oldDataSource = DataSourceDataHolder.mapFromDTODataSourceEntity(connection.getAllDataSources().get(0));
            SyncProgressTrackingData syncResults = dataSyncService.reSyncDataSource(orgId, oldDataSource, connection);
//            check we processed 5 rows
            Assertions.assertEquals(5, syncResults.getProcessed());
//            retrieve all existing rows
            List<Map<String, Object>> allRows = cassandraSyncRepository.selectAllToMap(orgId, oldDataSource);
//            check that existing rows count match processed ones
            Assertions.assertEquals(allRows.size(), syncResults.getProcessed());
//            check that columns count in table is correct
            Assertions.assertEquals(allRows.get(0).keySet().size(), 52);

        }

        Mockito.doReturn(parseJsonFile("mocks/mailchimp/new_mailchimps_members.json", new TypeReference<MembersWrapper>() {}))
                .when(restService).execute(Mockito.any(), Mockito.any(MailChimpMembersRequest.class), Mockito.any());

        Mockito.doReturn(parseJsonFile("mocks/mailchimp/new_mailchimps_merge_fields.json", new TypeReference<Map<String, Object>>() {}))
                .when(restService).execute(Mockito.any(), Mockito.any(MailChimpMergeFieldsRequest.class), Mockito.any());
        try (AbstractConnection connection = connectionFactory.buildConnection(connectionDTO)) {
            SyncProgressTrackingData syncResults = dataSyncService.reSyncDataSource(orgId, oldDataSource, connection);
            DataSourceDataHolder newDataSource = DataSourceDataHolder.mapFromDTODataSourceEntity(connection.getAllDataSources().get(0));
//            check we processed 5 rows
            Assertions.assertEquals(5, syncResults.getProcessed());
//            check we updated all 5 rows
            Assertions.assertEquals(5, syncResults.getUpdated());

            List<Map<String, Object>> allRows = cassandraSyncRepository.selectAllToMap(orgId, newDataSource);
//            check that existing rows count match processed ones
            Assertions.assertEquals(allRows.size(), syncResults.getProcessed());
            //            check that columns count in table is correct
            Assertions.assertEquals(allRows.get(0).keySet().size(), 47);

        }


    }

    @Test
    public void testSimpleSync() throws Exception {
        DTOConnection connectionDTO = defaultConnection();
        long orgId = 30;

        try (AbstractConnection connection = connectionFactory.buildConnection(connectionDTO)) {
            connection.getAllDataSources()
                    .stream()
                    .map(DataSourceDataHolder::mapFromDTODataSourceEntity)
                    .forEach(dataSource -> dataSyncService.reSyncDataSource(orgId, dataSource, connection));
        }


    }

    private DTOConnection defaultConnection() {
        DTOConnection dtoConnection = new DTOConnection();
        dtoConnection.setConnectionType(ConnectionType.MAILCHIMP);
        ConnectionSettings connectionSettings = new ConnectionSettings();
        connectionSettings.setApiKey(DEFAULT_API_KEY);
        dtoConnection.setConnectionSettings(connectionSettings);

        return dtoConnection;
    }

    private <T> T parseJsonFile(String path, TypeReference<T> tr) {
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(path);

        try {
            return JsonDataConverter.getInstance().fromString(IOUtils.toString(inputStream, "UTF-8"), tr);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}
