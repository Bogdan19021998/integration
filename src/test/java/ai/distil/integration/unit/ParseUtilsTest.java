package ai.distil.integration.unit;

import ai.distil.integration.job.sync.SyncTableDefinition;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static ai.distil.model.types.DataSourceSchemaAttributeTag.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class ParseUtilsTest {


    @Test
    public void testAttributesTags() {
        SyncTableDefinition customerDef = SyncTableDefinition.CUSTOMER;

        Assertions.assertEquals(customerDef.tryToGetAttributeType("id"), CUSTOMER_EXTERNAL_ID);
        Assertions.assertEquals(customerDef.tryToGetAttributeType("giVen_n_a_m_e"), CUSTOMER_FIRST_NAME);

    }
}
