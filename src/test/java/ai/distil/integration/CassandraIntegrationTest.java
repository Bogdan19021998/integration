package ai.distil.integration;

import ai.distil.api.internal.model.dto.DTODataSourceAttribute;
import ai.distil.integration.cassandra.repository.CassandraSyncRepository;
import ai.distil.integration.job.sync.holder.DataSourceDataHolder;
import ai.distil.model.types.CassandraDataSourceAttributeType;
import ai.distil.model.types.DataSourceSchemaAttributeTag;
import ai.distil.model.types.DataSourceType;
import com.google.common.collect.Lists;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Date;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class CassandraIntegrationTest {

    @Autowired
    private CassandraSyncRepository cassandraSyncRepository;

    @Test
    public void createTableTest() {
        String tenantId = "2";

        DataSourceDataHolder holder = new DataSourceDataHolder("test", "test",
                Lists.newArrayList(
                        new DTODataSourceAttribute(null, "id", "id", "c1", CassandraDataSourceAttributeType.BIGINT.getAttributeType(), CassandraDataSourceAttributeType.BIGINT, true, DataSourceSchemaAttributeTag.CUSTOMER_EXTERNAL_ID, true, new Date(), new Date(), null, null),
                        new DTODataSourceAttribute(null, "name", "name", "c2", CassandraDataSourceAttributeType.STRING.getAttributeType(), CassandraDataSourceAttributeType.STRING, true, DataSourceSchemaAttributeTag.NONE, true, new Date(), new Date(), null, null),
                        new DTODataSourceAttribute(null, "testBool", "testBool", "c3", CassandraDataSourceAttributeType.BOOLEAN.getAttributeType(), CassandraDataSourceAttributeType.BOOLEAN, true, DataSourceSchemaAttributeTag.NONE, true, new Date(), new Date(), null, null),
                        new DTODataSourceAttribute(null, "testDate", "testDate", "c4", CassandraDataSourceAttributeType.DATE.getAttributeType(), CassandraDataSourceAttributeType.DATE, true, DataSourceSchemaAttributeTag.NONE, true, new Date(), new Date(), null, null),
                        new DTODataSourceAttribute(null, "testDouble", "testDouble", "c5", CassandraDataSourceAttributeType.DOUBLE.getAttributeType(), CassandraDataSourceAttributeType.DOUBLE, true, DataSourceSchemaAttributeTag.NONE, true, new Date(), new Date(), null, null)
                ), DataSourceType.CUSTOMER, null);

        cassandraSyncRepository.createTableIfNotExists(tenantId, holder, true);


    }

}

