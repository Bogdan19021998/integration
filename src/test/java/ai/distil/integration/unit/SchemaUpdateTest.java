package ai.distil.integration.unit;

import ai.distil.api.internal.model.dto.DTODataSourceAttribute;
import ai.distil.integration.job.sync.holder.DataSourceDataHolder;
import ai.distil.integration.service.SchemaSyncService;
import ai.distil.integration.service.vo.AttributeChangeInfo;
import ai.distil.integration.service.vo.AttributeChangeType;
import ai.distil.integration.utils.ListUtils;
import ai.distil.integration.utils.func.FunctionChecked;
import ai.distil.model.types.DataSourceAttributeType;
import ai.distil.model.types.DataSourceSchemaAttributeTag;
import ai.distil.model.types.DataSourceType;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class SchemaUpdateTest {

    @Autowired
    private SchemaSyncService schemaSyncService;

    @Test
    public void schemaChangesDetectionTest() {

        List<DTODataSourceAttribute> oldAttributes = Lists.newArrayList(
                buildSimpleDataSourceAttribute("c1", DataSourceAttributeType.BIGINT),
                buildSimpleDataSourceAttribute("c2", DataSourceAttributeType.BIGINT),
                buildSimpleDataSourceAttribute("c3", DataSourceAttributeType.BIGINT),
                buildSimpleDataSourceAttribute("c4", DataSourceAttributeType.BIGINT)
        );


        List<DTODataSourceAttribute> newAttributes = Lists.newArrayList(
                buildSimpleDataSourceAttribute("c1", DataSourceAttributeType.DATE),
                buildSimpleDataSourceAttribute("c2", DataSourceAttributeType.BIGINT),
                buildSimpleDataSourceAttribute("c3", DataSourceAttributeType.BIGINT),
                buildSimpleDataSourceAttribute("c4444", DataSourceAttributeType.BIGINT)
        );

        FunctionChecked<DTODataSourceAttribute, String> getAttributeSourceName = DTODataSourceAttribute::getAttributeSourceName;

        DataSourceDataHolder oldSchema = new DataSourceDataHolder("test", "test", oldAttributes, DataSourceType.CUSTOMER, 1L);
        DataSourceDataHolder newSchema = new DataSourceDataHolder("test", "test", newAttributes, DataSourceType.CUSTOMER, 1L);


        List<AttributeChangeInfo> attributeChangeInfos = schemaSyncService.defineSchemaChanges(oldSchema, newSchema);

        Map<String, AttributeChangeType> result = new TreeMap<>(ListUtils.groupByWithOverwrite(attributeChangeInfos,
                v -> Optional.ofNullable(v.getNewAttribute()).orElse(v.getOldAttribute()).getAttributeSourceName(),
                AttributeChangeInfo::getAttributeChangeType));

        Map<String, AttributeChangeType> expectedResult = new TreeMap<>(ImmutableMap.of(
                "c1", AttributeChangeType.TYPE_CHANGED,
                "c2", AttributeChangeType.NOT_CHANGED,
                "c3", AttributeChangeType.NOT_CHANGED,
                "c4", AttributeChangeType.DELETED,
                "c4444", AttributeChangeType.ADDED
        ));

        Assertions.assertEquals(expectedResult, result);
    }

    private DTODataSourceAttribute buildSimpleDataSourceAttribute(String sourceName, DataSourceAttributeType attributeType) {
        DTODataSourceAttribute res = new DTODataSourceAttribute();
        res.setAttributeSourceName(sourceName);
        res.setAttributeDistilName(sourceName);
        res.setAttributeType(attributeType);
        res.setAttributeDataTag(DataSourceSchemaAttributeTag.NONE);
        res.setSyncAttribute(false);
        res.setVerifiedStillPresent(true);

        return res;
    }

}
