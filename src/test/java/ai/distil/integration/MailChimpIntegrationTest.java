package ai.distil.integration;

import ai.distil.api.internal.model.dto.DTOConnection;
import ai.distil.api.internal.model.dto.DTODataSource;
import ai.distil.integration.job.sync.http.mailchimp.MailChimpHttpConnection;
import ai.distil.model.org.ConnectionSettings;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class MailChimpIntegrationTest {

    @Test
    public void simpleMailChimpHttpConnectionTest() {
        MailChimpHttpConnection connection = new MailChimpHttpConnection(defaultConnection());

        List<DTODataSource> allDataSources = connection.getAllDataSources();
    }

    private DTOConnection defaultConnection() {
        DTOConnection dtoConnection = new DTOConnection();
        ConnectionSettings connectionSettings = new ConnectionSettings();
        connectionSettings.setApiKey("YXBpOmE4ZjlkMTBjYmVlODZhNjM3N2Q2YTliNWM3Yzg5NThlLXVzMw==");
        dtoConnection.setConnectionSettings(connectionSettings);

        return dtoConnection;
    }
}
