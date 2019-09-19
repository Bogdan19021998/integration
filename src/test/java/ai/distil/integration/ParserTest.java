package ai.distil.integration;

import ai.distil.api.internal.model.dto.DTOConnection;
import ai.distil.api.internal.model.dto.DTODataSource;
import ai.distil.api.internal.proxy.DataSourceProxy;
import ai.distil.integration.job.sync.AbstractConnection;
import ai.distil.integration.service.sync.ConnectionFactory;
import ai.distil.model.org.ConnectionSettings;
import ai.distil.model.types.ConnectionType;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class ParserTest {

    @MockBean
    @Autowired
    private DataSourceProxy dataSourceProxy;

    @Autowired
    private ConnectionFactory connectionFactory;

    @Test
    @Disabled
//  mysql vagrant instance must be run, check 'development/mysql' folder
//  ssh port may vary

    public void mySqlSshTest() throws IOException {

        String sshKeyPath = "development/mysql-vagrant/.vagrant/machines/mysql57/virtualbox/private_key";

        String key = IOUtils.toString(new FileInputStream(sshKeyPath));


        DTOConnection connectionDTO = new DTOConnection();
        connectionDTO.setConnectionType(ConnectionType.MYSQL);
        connectionDTO.setConnectionSettings(new ConnectionSettings(
                "root",
                "Passw0rd!",
                null,
                null,
                null,
                "localhost", null, null,
                "3306", "mysql",
                null,
                true, "localhost", 2222, "vagrant", null, key,
                null
        ));

        try (AbstractConnection connection = connectionFactory.buildConnection(connectionDTO)) {
            List<DTODataSource> allDataSources = connection.getAllDataSources();

//          check that default mysql getSchema - "mysql" has 31 tables
            Assertions.assertEquals(allDataSources.size(), 31);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    @Disabled
//  mysql vagrant instance must be run, check 'development/mysql' folder
//  ssh port may vary
    public void mySqlSshByUserNameAndPasswordTest() throws IOException {
        DTOConnection connectionDTO = new DTOConnection();
        connectionDTO.setConnectionType(ConnectionType.MYSQL);
        connectionDTO.setConnectionSettings(new ConnectionSettings(
                "root",
                "Passw0rd!",
                null,
                null,
                null, "localhost",
                null,
                null,
                "3306",
                "mysql", null,
                true, "localhost", 2214, "vagrant", "vagrant", null,
                null
        ));

        try (AbstractConnection connection = connectionFactory.buildConnection(connectionDTO)) {
            List<DTODataSource> allDataSources = connection.getAllDataSources();

//          check that default mysql getSchema - "mysql" has 31 tables
            Assertions.assertEquals(allDataSources.size(), 31);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}
