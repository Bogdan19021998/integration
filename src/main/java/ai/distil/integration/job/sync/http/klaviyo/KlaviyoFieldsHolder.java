package ai.distil.integration.job.sync.http.klaviyo;

import ai.distil.integration.job.sync.http.IFieldsHolder;
import ai.distil.integration.job.sync.http.mailchimp.SimpleDataSourceField;
import ai.distil.model.types.CassandraDataSourceAttributeType;
import ai.distil.model.types.DataSourceSchemaAttributeTag;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class KlaviyoFieldsHolder implements IFieldsHolder {


    @Override
    public Map<String, CassandraDataSourceAttributeType> getDataTypeMapping() {
        return null;
    }

    @Override
    public Map<DataSourceSchemaAttributeTag, Set<String>> getAttributesTagsMappingByName() {
        return null;
    }

    @Override
    public List<SimpleDataSourceField> getStaticDataSourceFields() {
        return null;
    }

    @Override
    public List<SimpleDataSourceField> getDynamicDataSourceField(Object fieldDefinition) {
        return null;
    }
}
