package ai.distil.integration;

import ai.distil.api.internal.model.dto.DTOConnection;
import ai.distil.api.internal.model.dto.DTODataSource;
import ai.distil.integration.controller.dto.data.DatasetRow;
import ai.distil.integration.job.sync.holder.DataSourceDataHolder;
import ai.distil.integration.job.sync.http.mailchimp.MailChimpHttpConnection;
import ai.distil.integration.job.sync.iterator.HttpPaginationRowIterator;
import ai.distil.integration.job.sync.jdbc.SimpleDataSourceDefinition;
import ai.distil.model.org.ConnectionSettings;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class MailChimpIntegrationTest {

    public static final String DEFAULT_CLIENT_ID = "3e3e1502d7";

    @Test
    public void simpleMailChimpHttpConnectionTest() {
        MailChimpHttpConnection connection = new MailChimpHttpConnection(defaultConnection());

        DTODataSource existDataSource = connection.getAllDataSources().stream().filter(v -> DEFAULT_CLIENT_ID.equals(v.getSourceTableName())).findFirst().get();
        Assertions.assertNotNull(existDataSource);

        DataSourceDataHolder dataHolder = DataSourceDataHolder.mapFromDTODataSourceEntity(existDataSource);

        HttpPaginationRowIterator httpPaginationRowIterator = new HttpPaginationRowIterator(connection, dataHolder, 2);
        List<DatasetRow> rows = new ArrayList<>(Lists.newArrayList(httpPaginationRowIterator));
        Assertions.assertEquals(rows.size(), 5);
    }

    @Test
    public void checkSourceAvailabilityTest() {
        DTOConnection dtoConnection = defaultConnection();
        MailChimpHttpConnection connection = new MailChimpHttpConnection(dtoConnection);

        Assertions.assertTrue(connection.isAvailable());

        dtoConnection.getConnectionSettings().setApiKey("somefakeapikey");

        Assertions.assertFalse(connection.isAvailable());
    }

    @Test
    public void getSingleDataSourceTest() {

        DTOConnection dtoConnection = defaultConnection();
        MailChimpHttpConnection connection = new MailChimpHttpConnection(dtoConnection);
        DTODataSource existDataSource = connection.getAllDataSources().stream().filter(v -> DEFAULT_CLIENT_ID.equals(v.getSourceTableName())).findFirst().get();

        DTODataSource dataSource = connection.getDataSource(new SimpleDataSourceDefinition(DEFAULT_CLIENT_ID, null, null, null));

        Assertions.assertNotNull(dataSource);
        Assertions.assertTrue(connection.dataSourceExist(DataSourceDataHolder.mapFromDTODataSourceEntity(existDataSource)));


    }

    private DTOConnection defaultConnection() {
        DTOConnection dtoConnection = new DTOConnection();
        ConnectionSettings connectionSettings = new ConnectionSettings();
        connectionSettings.setApiKey("YXBpOmE4ZjlkMTBjYmVlODZhNjM3N2Q2YTliNWM3Yzg5NThlLXVzMw==");
        dtoConnection.setConnectionSettings(connectionSettings);

        return dtoConnection;
    }
}
