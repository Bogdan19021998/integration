package ai.distil.integration.job.sync.http;

import ai.distil.integration.job.sync.http.mailchimp.SimpleDataSourceField;
import ai.distil.integration.utils.MapUtils;
import ai.distil.model.types.DataSourceAttributeType;
import ai.distil.model.types.DataSourceSchemaAttributeTag;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ai.distil.integration.utils.NamingUtils.sanitizeColumnName;

//T is a type for dynamic fields definitions
public interface IFieldsHolder<T> {
    String DEFAULT_TYPE_KEY = "DISTIL_DEFAULT_KEY";


    //  map for types mappings, e.g. number -> BIGINT, double -> NUMBER, etc...
    Map<String, DataSourceAttributeType> getDataTypeMapping();

    //  key - attribute tag, value - set of eligible fields names
    Map<DataSourceSchemaAttributeTag, Set<String>> getAttributesTagsMappingByName();

    default Map<DataSourceSchemaAttributeTag, Set<String>> getAttributesTagsMappingByType() {
        return Collections.emptyMap();
    }

    default Set<String> getExcludeFields() {
        return Collections.emptySet();
    }

    default DataSourceAttributeType defineType(String type) {
        return getDataTypeMapping().getOrDefault(type, DataSourceAttributeType.UNKNOWN);
    }

    default DataSourceSchemaAttributeTag tryToDefineTag(String fieldName, String columnType) {
        return defineTag(fieldName, getAttributesTagsMappingByName())
                .orElseGet(() -> defineTag(columnType, getAttributesTagsMappingByType())
                        .orElse(null));

    }

    default Optional<DataSourceSchemaAttributeTag> defineTag(String text, Map<DataSourceSchemaAttributeTag, Set<String>> definition) {
        return Optional.ofNullable(text)
                .map(field ->
                        definition.entrySet().stream()
                                .filter(tagDefinition -> tagDefinition.getValue().contains(sanitizeColumnName(field)))
                                .findFirst()
                                .map(Map.Entry::getKey)
                                .orElse(null));
    }

    List<SimpleDataSourceField> getStaticDataSourceFields();

    //  it returns a list, because it's possible to produce a list of fields from the on field
    List<SimpleDataSourceField> getDynamicDataSourceField(T fieldDefinition);

    default List<SimpleDataSourceField> getDynamicDataSourceFields(List<T> fieldsDefinition) {
        return fieldsDefinition.stream()
                .flatMap(fd -> this.getDynamicDataSourceField(fd).stream())
                .collect(Collectors.toList());
    }

    default List<SimpleDataSourceField> getAllFields(List<T> dynamicFields) {
        return Stream.concat(getStaticDataSourceFields().stream(), getDynamicDataSourceFields(dynamicFields).stream())
                .collect(Collectors.toList());
    }

    default SimpleDataSourceField buildSimpleField(String parentPath, String fieldName, String displayName,
                                                   DataSourceAttributeType attributeType) {
        return new SimpleDataSourceField(buildFieldName(parentPath, fieldName), displayName, attributeType, tryToDefineTag(fieldName, null));
    }

    default String buildFieldName(String path, String fieldName) {
        return MapUtils.buildKeyName(path, fieldName);
    }
}
