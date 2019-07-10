package ai.distil.integration;

import ai.distil.api.internal.model.dto.DTOConnection;
import ai.distil.api.internal.model.dto.DTODataSource;
import ai.distil.integration.job.sync.http.mailchimp.MailChimpHttpConnection;
import ai.distil.integration.job.sync.iterator.HttpPaginationRowIterator;
import ai.distil.model.org.ConnectionSettings;
import ai.distil.model.types.DataSourceType;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class MailChimpIntegrationTest {

    @Test
    public void simpleMailChimpHttpConnectionTest() {
        MailChimpHttpConnection connection = new MailChimpHttpConnection(defaultConnection());

        List<DTODataSource> allDataSources = connection.getAllDataSources();
        DTODataSource dtoDataSource = new DTODataSource();
        dtoDataSource.setSourceTableName("3e3e1502d7");
        dtoDataSource.setDataSourceType(DataSourceType.CUSTOMER);

        HttpPaginationRowIterator httpPaginationRowIterator = new HttpPaginationRowIterator(connection, dtoDataSource, 2);
        httpPaginationRowIterator.forEachRemaining(row -> {
            log.info("Row: {}", row);
        });
    }

    @Test
    public void checkSourceAvailabilityTest() {
        DTOConnection dtoConnection = defaultConnection();
        MailChimpHttpConnection connection = new MailChimpHttpConnection(dtoConnection);

        Assertions.assertTrue(connection.isAvailable());

        dtoConnection.getConnectionSettings().setApiKey("somefakeapikey");

        Assertions.assertFalse(connection.isAvailable());
    }

    private DTOConnection defaultConnection() {
        DTOConnection dtoConnection = new DTOConnection();
        ConnectionSettings connectionSettings = new ConnectionSettings();
        connectionSettings.setApiKey("YXBpOmE4ZjlkMTBjYmVlODZhNjM3N2Q2YTliNWM3Yzg5NThlLXVzMw==");
        dtoConnection.setConnectionSettings(connectionSettings);

        return dtoConnection;
    }
}
