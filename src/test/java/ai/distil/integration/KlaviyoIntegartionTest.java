package ai.distil.integration;

import ai.distil.api.internal.model.dto.DTOConnection;
import ai.distil.integration.cassandra.repository.CassandraSyncRepository;
import ai.distil.integration.job.sync.AbstractConnection;
import ai.distil.integration.job.sync.holder.DataSourceDataHolder;
import ai.distil.integration.service.DataSyncService;
import ai.distil.integration.service.sync.ConnectionFactory;
import ai.distil.model.org.ConnectionSettings;
import ai.distil.model.types.ConnectionType;
import com.datastax.driver.core.schemabuilder.SchemaBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class KlaviyoIntegartionTest {


    @Autowired
    private CassandraSyncRepository cassandraSyncRepository;

    @Autowired
    private ConnectionFactory connectionFactory;

    @Autowired
    private DataSyncService dataSyncService;

    private final String DEFAULT_API_KEY = "pk_c573b131433429f49daef8ea6380147e97";

    @Test
    public void testGetKlavioSimpleSync() throws Exception {
        DTOConnection connectionDTO = defaultConnection();
        String tenantId = "60";

        cassandraSyncRepository.getConnection().getSession().execute(SchemaBuilder.dropKeyspace(String.format("distil_org_%s", tenantId)).ifExists());

        try (AbstractConnection connection = connectionFactory.buildConnection(connectionDTO)) {
            connection.getAllDataSources()
                    .stream()
                    .map(DataSourceDataHolder::mapFromDTODataSourceEntity)
                    .forEach(dataSource -> dataSyncService.reSyncDataSource(tenantId, dataSource, connection));
        }
    }

    private DTOConnection defaultConnection() {
        DTOConnection dtoConnection = new DTOConnection();
        dtoConnection.setConnectionType(ConnectionType.REDSHIFT);
        ConnectionSettings connectionSettings = new ConnectionSettings();
        connectionSettings.setApiKey(DEFAULT_API_KEY);
        dtoConnection.setConnectionSettings(connectionSettings);
        return dtoConnection;
    }
}
