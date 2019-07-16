package ai.distil.integration.job.sync.http.campmon.holder;

import ai.distil.integration.controller.dto.data.DatasetRow;
import ai.distil.integration.job.sync.http.IFieldsHolder;
import ai.distil.integration.job.sync.http.campmon.vo.CustomFieldDefinition;
import ai.distil.integration.job.sync.http.mailchimp.SimpleDataSourceField;
import ai.distil.integration.utils.ListUtils;
import ai.distil.model.types.DataSourceAttributeType;
import ai.distil.model.types.DataSourceSchemaAttributeTag;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.buf.StringUtils;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Component
public class CampaignMonitorFieldsHolder implements IFieldsHolder<List<CustomFieldDefinition>> {
    private static final String CUSTOM_FIELDS_KEY = "CustomFields";
    private static final String KEY_FIELD = "Key";
    private static final String VALUE_FIELD = "Value";

    private static final Map<String, DataSourceAttributeType> DATA_TYPE_MAPPING = new HashMap<String, DataSourceAttributeType>() {{
        this.put("Text", DataSourceAttributeType.STRING);
// campaign monitor returns data as strings, but type is number, try to deal with it
//        todo add transformer
        this.put("Number", DataSourceAttributeType.STRING);
        this.put("MultiSelectOne", DataSourceAttributeType.STRING);
        this.put("Date", DataSourceAttributeType.STRING);
        this.put("MultiSelectMany", DataSourceAttributeType.STRING);
    }};

    private static final Map<DataSourceSchemaAttributeTag, Set<String>> TAGS_MAPPING_FOR_NAMES = new HashMap<DataSourceSchemaAttributeTag, Set<String>>() {{
        this.put(DataSourceSchemaAttributeTag.PRIMARY_KEY, Sets.newHashSet("EMAILADDRESS"));
        this.put(DataSourceSchemaAttributeTag.CUSTOMER_POSTCODE, Sets.newHashSet("POSTCODE", "ZIP"));
        this.put(DataSourceSchemaAttributeTag.CUSTOMER_FIRST_NAME, Sets.newHashSet("FNAME", "FIRSTNAME", "GIVENNAME"));
        this.put(DataSourceSchemaAttributeTag.CUSTOMER_LAST_NAME, Sets.newHashSet("LNAME", "LASTNAME", "SURNAME"));
        this.put(DataSourceSchemaAttributeTag.CUSTOMER_MOBILE_NUMBER, Sets.newHashSet("PHONE", "MOBILENUMBER", "TELEPHONE", "TELNUMBER", "PHONENUMBER"));
        this.put(DataSourceSchemaAttributeTag.CUSTOMER_COUNTRY_CODE, Sets.newHashSet("COUNTRY", "COUNTRYCODE"));
    }};

    private static final Map<DataSourceSchemaAttributeTag, Set<String>> TAGS_MAPPING_FOR_TYPES = new HashMap<DataSourceSchemaAttributeTag, Set<String>>() {{
        this.put(DataSourceSchemaAttributeTag.CUSTOMER_POSTCODE, Sets.newHashSet("Zip Code", "Postal Code"));
        this.put(DataSourceSchemaAttributeTag.CUSTOMER_COUNTRY_CODE, Sets.newHashSet("Country"));
    }};

    private static final Map<String, Function<Map<String, Object>, Map<String, Object>>> CUSTOM_FIELDS_TRANSFORMERS = new HashMap<String, Function<Map<String, Object>, Map<String, Object>>>() {{
        this.put(CUSTOM_FIELDS_KEY, row -> Optional.ofNullable((List<Map<String, Object>>) row.get(CUSTOM_FIELDS_KEY)).map(customFields -> {
            Map<String, Object> result = new HashMap<>();
            Map<String, List<String>> fields = ListUtils.groupBy(customFields,
                    o -> String.valueOf(o.get(KEY_FIELD)),
                    o -> String.valueOf(o.get(VALUE_FIELD)));
            fields.forEach((key, values) -> result.put(key, StringUtils.join(values, ',')));
            return result;
        }).orElse(Collections.emptyMap()));
    }};


    private final List<SimpleDataSourceField> DEFAULT_STATIC_FIELDS = Stream.of("EmailAddress", "Name", "Date", "State", "ReadsEmailWith")
            .map(field -> buildSimpleField(null, field, field, DataSourceAttributeType.STRING))
            .collect(Collectors.toList());

    public DatasetRow transformRow(Map<String, Object> row) {
        DatasetRow.DatasetRowBuilder builder = new DatasetRow.DatasetRowBuilder();

        row.forEach((key, value) -> {
            if(CUSTOM_FIELDS_TRANSFORMERS.containsKey(key)) {
                Optional.ofNullable(CUSTOM_FIELDS_TRANSFORMERS.get(key).apply(row))
                        .ifPresent(r -> r.forEach(builder::addValue));
            } else {
                builder.addValue(key, value);
            }
        });
        return builder.build();
    }

    @Override
    public Set<String> getExcludeFields() {
        return Collections.emptySet();
    }

    @Override
    public Map<String, DataSourceAttributeType> getDataTypeMapping() {
        return DATA_TYPE_MAPPING;
    }

    @Override
    public Map<DataSourceSchemaAttributeTag, Set<String>> getAttributesTagsMappingByName() {
        return TAGS_MAPPING_FOR_NAMES;
    }

    @Override
    public Map<DataSourceSchemaAttributeTag, Set<String>> getAttributesTagsMappingByType() {
        return TAGS_MAPPING_FOR_TYPES;
    }

    @Override
    public List<SimpleDataSourceField> getStaticDataSourceFields() {
        return DEFAULT_STATIC_FIELDS;
    }

    @Override
    public List<SimpleDataSourceField> getDynamicDataSourceFields(List<CustomFieldDefinition> fieldsDefinition) {
        return fieldsDefinition.stream()
                .map(field -> buildSimpleField(null, field.getKey(), field.getFieldName(), defineType(field.getDataType())))
                .collect(Collectors.toList());
    }
}
