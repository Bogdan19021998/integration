package ai.distil.integration;

import ai.distil.api.internal.proxy.DataSourceProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class AbstractIntegrationTest {
    @MockBean
    @Autowired
    private DataSourceProxy dataSourceProxy;
}
