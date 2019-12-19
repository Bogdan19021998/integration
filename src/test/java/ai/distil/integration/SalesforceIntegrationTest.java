package ai.distil.integration;

import ai.distil.api.internal.model.dto.DTOConnection;
import ai.distil.api.internal.model.dto.DTODataSource;
import ai.distil.integration.cassandra.repository.CassandraSyncRepository;
import ai.distil.integration.job.sync.AbstractConnection;
import ai.distil.integration.job.sync.holder.DataSourceDataHolder;
import ai.distil.integration.job.sync.http.sf.request.SalesforceDataRequest;
import ai.distil.integration.job.sync.http.sf.request.SalesforceListFieldsRequest;
import ai.distil.integration.job.sync.http.sf.request.SalesforceLoginRequest;
import ai.distil.integration.job.sync.http.sf.vo.SalesforceDataPage;
import ai.distil.integration.job.sync.http.sf.vo.SalesforceListFields;
import ai.distil.integration.job.sync.http.sf.vo.SalesforceLoginResponse;
import ai.distil.integration.controller.dto.destination.SyncProgressTrackingData;
import ai.distil.integration.service.DataSyncService;
import ai.distil.integration.service.RestService;
import ai.distil.integration.service.sync.ConnectionFactory;
import ai.distil.model.org.ConnectionSettings;
import ai.distil.model.types.ConnectionType;
import com.datastax.driver.core.schemabuilder.SchemaBuilder;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static ai.distil.integration.utils.ParseUtils.parseJsonFile;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class SalesforceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private ConnectionFactory connectionFactory;

    @Autowired
    private CassandraSyncRepository cassandraSyncRepository;

    @Autowired
    private DataSyncService dataSyncService;

    @SpyBean
    @Autowired
    private RestService restService;

    @Test
    public void checkSalesforceConnectionTest() {
        DTOConnection connectionDto = getDefaultDtoConnection();

        try(AbstractConnection connection = connectionFactory.buildConnection(connectionDto)) {
            Assertions.assertTrue(connection.isAvailable());
            connectionDto.getConnectionSettings().setUserName("fakeusername");
            Assertions.assertFalse(connection.isAvailable());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void salesforceGetAllDataSourcesTest() {
        DTOConnection connectionDto = getDefaultDtoConnection();

        try(AbstractConnection connection = connectionFactory.buildConnection(connectionDto)) {
            List<DTODataSource> allDataSources = connection.getAllDataSources();
            Assertions.assertEquals(2, allDataSources.size());
            Assertions.assertEquals(137, allDataSources.get(0).getAttributes().size());
            Assertions.assertEquals(481, allDataSources.get(1).getAttributes().size());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
//    disabled it, because there are 35k rows
    @Disabled
    public void salesforceSyncLeadDataSource() {
        String tenantId = "123";
        cassandraSyncRepository.getConnection().getSession()
                .execute(SchemaBuilder.dropKeyspace(String.format("%s%s", CassandraSyncRepository.KEYSPACE_PREFIX, tenantId))
                .ifExists());

        DTOConnection connectionDto = getDefaultDtoConnection();
        String dataSourceId = "Lead";

        try(AbstractConnection connection = connectionFactory.buildConnection(connectionDto)) {
            List<DTODataSource> allDataSources = connection.getAllDataSources();
            DataSourceDataHolder dataSource = allDataSources.stream().filter(d -> d.getSourceTableName().equals(dataSourceId)).findFirst()
                    .map(DataSourceDataHolder::mapFromDTODataSourceEntity)
                    .orElseThrow(() -> new RuntimeException("There is no lead datasource here"));

            dataSyncService.reSyncDataSource(tenantId, dataSource, connection);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    //    disabled it, because there are 35k rows
    @Disabled
    public void salesforceSyncContactDataSource() {
        String tenantId = "123";
        cassandraSyncRepository.getConnection().getSession()
                .execute(SchemaBuilder.dropKeyspace(String.format("%s%s", CassandraSyncRepository.KEYSPACE_PREFIX, tenantId))
                .ifExists());

        DTOConnection connectionDto = getDefaultDtoConnection();
        String dataSourceId = "Contact";

        try(AbstractConnection connection = connectionFactory.buildConnection(connectionDto)) {
            List<DTODataSource> allDataSources = connection.getAllDataSources();
            DataSourceDataHolder dataSource = allDataSources.stream().filter(d -> d.getSourceTableName().equals(dataSourceId)).findFirst()
                    .map(DataSourceDataHolder::mapFromDTODataSourceEntity)
                    .orElseThrow(() -> new RuntimeException("There is no lead datasource here"));

            SyncProgressTrackingData syncResult = dataSyncService.reSyncDataSource(tenantId, dataSource, connection);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void reSyncsalesforceContactDataSource() {
        String tenantId = "123";
        cassandraSyncRepository.getConnection().getSession()
                .execute(SchemaBuilder.dropKeyspace(String.format("%s%s", CassandraSyncRepository.KEYSPACE_PREFIX, tenantId))
                        .ifExists());

        Mockito.doReturn(parseJsonFile("mocks/salesforce/sf_login_response.json", new TypeReference<SalesforceLoginResponse>() {}))
                .when(restService).execute(Mockito.any(), Mockito.any(SalesforceLoginRequest.class), Mockito.any());

        Mockito.doReturn(parseJsonFile("mocks/salesforce/sf_leads_data.json", new TypeReference<SalesforceDataPage>() {}))
                .when(restService).execute(Mockito.any(), Mockito.any(SalesforceDataRequest.class), Mockito.any());

        Mockito.doReturn(parseJsonFile("mocks/salesforce/sf_leads_fields.json", new TypeReference<SalesforceListFields>() {}))
                .when(restService).execute(Mockito.any(), Mockito.any(SalesforceListFieldsRequest.class), Mockito.any());

        Mockito.doReturn(CompletableFuture.completedFuture(parseJsonFile("mocks/salesforce/sf_leads_fields.json", new TypeReference<SalesforceListFields>() {})))
                .when(restService).executeAsync(Mockito.any(), Mockito.any(SalesforceListFieldsRequest.class), Mockito.any());

        DTOConnection connectionDto = getDefaultDtoConnection();
        String dataSourceId = "Lead";
        DataSourceDataHolder dataSource;

        try(AbstractConnection connection = connectionFactory.buildConnection(connectionDto)) {
            List<DTODataSource> allDataSources = connection.getAllDataSources();
            dataSource = allDataSources.stream().filter(d -> d.getSourceTableName().equals(dataSourceId)).findFirst()
                    .map(DataSourceDataHolder::mapFromDTODataSourceEntity)
                    .orElseThrow(() -> new RuntimeException("There is no lead datasource here"));

            SyncProgressTrackingData syncResult = dataSyncService.reSyncDataSource(tenantId, dataSource, connection);

            List<Map<String, Object>> rows = cassandraSyncRepository.selectAllToMap(tenantId, dataSource);

            Assertions.assertEquals(200, syncResult.getProcessed());
            Assertions.assertEquals(200, syncResult.getCreated());
            Assertions.assertEquals(200, rows.size());

            Assertions.assertEquals(483, rows.get(0).keySet().size());

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

//        updated file contains new column, and has a removed row
        Mockito.doReturn(parseJsonFile("mocks/salesforce/new_sf_leads_data.json", new TypeReference<SalesforceDataPage>() {}))
                .when(restService).execute(Mockito.any(), Mockito.any(SalesforceDataRequest.class), Mockito.any());

        Mockito.doReturn(parseJsonFile("mocks/salesforce/new_sf_leads_fields.json", new TypeReference<SalesforceListFields>() {}))
                .when(restService).execute(Mockito.any(), Mockito.any(SalesforceListFieldsRequest.class), Mockito.any());

        try(AbstractConnection connection = connectionFactory.buildConnection(connectionDto)) {
            SyncProgressTrackingData syncResult = dataSyncService.reSyncDataSource(tenantId, dataSource, connection);
            List<Map<String, Object>> rows = cassandraSyncRepository.selectAllToMap(tenantId, dataSource);

            Assertions.assertEquals(199, syncResult.getProcessed());
            Assertions.assertEquals(199, rows.size());
            Assertions.assertEquals(482, rows.get(0).keySet().size());


        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private DTOConnection getDefaultDtoConnection() {
        DTOConnection dtoConnection = new DTOConnection();
        dtoConnection.setConnectionType(ConnectionType.SALESFORCE);
        ConnectionSettings connectionSettings = new ConnectionSettings();

        connectionSettings.setApiKey("3MVG9WtWSKUDG.x4wSF2d7e2LZgNq0RxH7LPRRgAGk.aAa74yuVS17mT4g2ibxVdEKB5nnJY2iliUgCgb9fkG");
        connectionSettings.setClientSecret("7656223653762781776");
        connectionSettings.setUserName("bigdata@big-consultancy.com");
        connectionSettings.setPassword("qj6B{Y8*NCe");
        connectionSettings.setSecurityCode("vkgzjlyw569RYSO6YMdy6YWBH");


        dtoConnection.setConnectionSettings(connectionSettings);

        return dtoConnection;
    }
}
