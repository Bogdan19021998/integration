package ai.distil.integration;

import ai.distil.api.internal.model.dto.DTODataSourceAttribute;
import ai.distil.integration.cassandra.repository.CassandraSyncRepository;
import ai.distil.integration.job.sync.holder.DataSourceDataHolder;
import ai.distil.model.types.DataSourceAttributeType;
import ai.distil.model.types.DataSourceSchemaAttributeTag;
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
                        new DTODataSourceAttribute(null, "id", "id", "c1", DataSourceAttributeType.BIGINT, true, DataSourceSchemaAttributeTag.PRIMARY_KEY, true, new Date(), new Date()),
                        new DTODataSourceAttribute(null, "name", "name", "c2", DataSourceAttributeType.STRING, true, null, true, new Date(), new Date()),
                        new DTODataSourceAttribute(null, "testBool", "testBool", "c3", DataSourceAttributeType.BOOLEAN, true, null, true, new Date(), new Date()),
                        new DTODataSourceAttribute(null, "testDate", "testDate", "c4", DataSourceAttributeType.DATE, true, null, true, new Date(), new Date()),
                        new DTODataSourceAttribute(null, "testDouble", "testDouble", "c5", DataSourceAttributeType.DOUBLE, true, null, true, new Date(), new Date())
                ), null, null);

        cassandraSyncRepository.createTableIfNotExists(tenantId, holder, true);


    }

}

