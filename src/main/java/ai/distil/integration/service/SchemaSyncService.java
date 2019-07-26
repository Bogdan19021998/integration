package ai.distil.integration.service;

import ai.distil.api.internal.model.dto.DTODataSourceAttribute;
import ai.distil.integration.job.sync.holder.DataSourceDataHolder;
import ai.distil.integration.service.vo.AttributeChangeInfo;
import ai.distil.integration.service.vo.AttributeChangeType;
import ai.distil.integration.utils.ListUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class SchemaSyncService {

    /**
     * method comparing schemas
     * returns attributes change info for all columns
     */
    public List<AttributeChangeInfo> defineSchemaChanges(DataSourceDataHolder oldSchema, DataSourceDataHolder newSchema) {
        Map<String, DTODataSourceAttribute> oldAttributesByName = ListUtils.groupByWithOverwrite(oldSchema.getAllAttributes(),
                DTODataSourceAttribute::getAttributeSourceName, true);

        Map<String, DTODataSourceAttribute> newAttributesByName = ListUtils.groupByWithOverwrite(newSchema.getAllAttributes(),
                DTODataSourceAttribute::getAttributeSourceName, true);

        List<AttributeChangeInfo> changedAttributesStream = oldAttributesByName.entrySet().stream().map(attr -> {
            String attributeSourceName = attr.getKey();
            DTODataSourceAttribute oldAttribute = attr.getValue();

//          removing attribute from the map, then after all iterations will be done only new attributes remains
            DTODataSourceAttribute newAttribute = newAttributesByName.remove(attributeSourceName);
//          todo this is a bad decision, but the easiest one, consider refactoring
            if(newAttribute != null) {
                newAttribute.setAttributeDistilName(oldAttribute.getAttributeDistilName());
            }

            AttributeChangeType attributeChangeType = AttributeChangeType.defineAttributeType(oldAttribute, newAttribute);
            return new AttributeChangeInfo(oldAttribute.getId(), oldAttribute, newAttribute, attributeChangeType);
        }).collect(Collectors.toList());


        List<AttributeChangeInfo> newAttributesStream = newAttributesByName.entrySet()
                .stream()
                .map(entry -> new AttributeChangeInfo(null, null, entry.getValue(), AttributeChangeType.ADDED))
                .collect(Collectors.toList());

        return Stream.concat(changedAttributesStream.stream(), newAttributesStream.stream())
                .collect(Collectors.toList());

    }

}
