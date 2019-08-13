package ai.distil.integration.job.sync.http;

import ai.distil.integration.controller.dto.data.DatasetRow;
import ai.distil.integration.job.sync.holder.DataSourceDataHolder;
import ai.distil.integration.job.sync.http.mailchimp.SimpleDataSourceField;
import ai.distil.integration.utils.MapUtils;
import ai.distil.integration.utils.StringUtils;
import ai.distil.model.types.CassandraDataSourceAttributeType;
import ai.distil.model.types.DataSourceSchemaAttributeTag;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ai.distil.integration.utils.NamingUtils.sanitizeColumnName;

//T is a type for dynamic fields definitions
public interface IFieldsHolder<T> {
    String DEFAULT_TYPE_KEY = "DISTIL_DEFAULT_KEY";

    //  map for types mappings, e.g. number -> BIGINT, double -> NUMBER, etc...
    Map<String, CassandraDataSourceAttributeType> getDataTypeMapping();

    //  key - attribute tag, value - set of eligible fields names
    Map<DataSourceSchemaAttributeTag, Set<String>> getAttributesTagsMappingByName();

    default Map<DataSourceSchemaAttributeTag, Set<String>> getAttributesTagsMappingByType() {
        return Collections.emptyMap();
    }

    default Set<String> getExcludeFields() {
        return Collections.emptySet();
    }


//  fields converter by name
    default Map<String, Function<?, Map<String, Object>>> getCustomFieldsConverters() {
        return Collections.emptyMap();
    }

//  fields converter by type
    default <K, V> Map<CassandraDataSourceAttributeType, Function<K, V>> getCustomTypeConverters() {
        return Collections.emptyMap();
    }

    default CassandraDataSourceAttributeType defineType(String type) {
        return getDataTypeMapping().getOrDefault(StringUtils.trimAndUppercase(type), CassandraDataSourceAttributeType.UNKNOWN);
    }

    default DataSourceSchemaAttributeTag tryToDefineTag(String fieldName, String columnType) {
        return defineTag(fieldName, getAttributesTagsMappingByName())
                .orElseGet(() -> defineTag(columnType, getAttributesTagsMappingByType())
                        .orElse(DataSourceSchemaAttributeTag.NONE));

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
                                                   CassandraDataSourceAttributeType attributeType) {
        return new SimpleDataSourceField(buildFieldName(parentPath, fieldName), displayName, attributeType, tryToDefineTag(fieldName, null));
    }

    default String buildFieldName(String path, String fieldName) {
        return MapUtils.buildKeyName(path, fieldName);
    }

    default DatasetRow transformRow (Map<String, Object> row, DataSourceDataHolder dataSource) {
        Map<String, Object> flattedRow = MapUtils.flatten(row, getCustomFieldsConverters());

        DatasetRow.DatasetRowBuilder builder = new DatasetRow.DatasetRowBuilder(flattedRow.size());
        Map<CassandraDataSourceAttributeType, Function<Object, Object>> customTypeMapper = getCustomTypeConverters();

        dataSource.getAllAttributes().forEach(attr -> {
            Object value = flattedRow.get(attr.getAttributeSourceName());
            Object transformedValue = Optional.ofNullable(customTypeMapper
                    .get(attr.getCassandraAttributeType()))
                    .map(f -> f.apply(value))
                    .orElse(value);

            builder.addValue(attr.getAttributeSourceName(), transformedValue);
        });

        return builder.build();
    }

    default Function<Object, Date> dateFormatter(String datePattern) {
        return value -> Optional.ofNullable(value).map((v) -> {
            DateFormat formatter = new SimpleDateFormat(datePattern);
            try {
                return formatter.parse(String.valueOf(v));
            } catch (ParseException e) {
                return null;
            }
        }).orElse(null);
    }
}
