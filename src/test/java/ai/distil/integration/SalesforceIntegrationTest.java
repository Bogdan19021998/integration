package ai.distil.integration;

import ai.distil.api.internal.model.dto.DTOConnection;
import ai.distil.integration.job.sync.AbstractConnection;
import ai.distil.integration.service.sync.ConnectionFactory;
import ai.distil.model.org.ConnectionSettings;
import ai.distil.model.types.ConnectionType;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class SalesforceIntegrationTest {

    @Autowired
    private ConnectionFactory connectionFactory;


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
