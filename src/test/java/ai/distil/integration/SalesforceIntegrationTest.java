package ai.distil.integration;

import ai.distil.api.internal.model.dto.DTOConnection;
import ai.distil.api.internal.model.dto.DTODataSource;
import ai.distil.integration.cassandra.repository.CassandraSyncRepository;
import ai.distil.integration.job.sync.AbstractConnection;
import ai.distil.integration.job.sync.holder.DataSourceDataHolder;
import ai.distil.integration.job.sync.progress.SyncProgressTrackingData;
import ai.distil.integration.service.DataSyncService;
import ai.distil.integration.service.sync.ConnectionFactory;
import ai.distil.model.org.ConnectionSettings;
import ai.distil.model.types.ConnectionType;
import com.datastax.driver.core.schemabuilder.SchemaBuilder;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class SalesforceIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private ConnectionFactory connectionFactory;

    @Autowired
    private CassandraSyncRepository cassandraSyncRepository;

    @Autowired
    private DataSyncService dataSyncService;

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
            Assertions.assertEquals(479, allDataSources.get(1).getAttributes().size());
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

            SyncProgressTrackingData syncResult = dataSyncService.reSyncDataSource(tenantId, dataSource, connection);
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
