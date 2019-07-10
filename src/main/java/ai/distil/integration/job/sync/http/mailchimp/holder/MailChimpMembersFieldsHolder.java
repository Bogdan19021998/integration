package ai.distil.integration.job.sync.http.mailchimp.holder;

import ai.distil.integration.job.sync.http.JsonDataConverter;
import ai.distil.integration.job.sync.http.mailchimp.SimpleDataSourceField;
import ai.distil.integration.utils.MapUtils;
import ai.distil.model.types.DataSourceAttributeType;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Component
public class MailChimpMembersFieldsHolder {

    private static final Set<String> EXCLUDE_FIELDS = Sets.newHashSet("_links", "merge_fields");


    private static final String ADDRESS_TYPE_KEY = "address";
    private static final String OBJECT_TYPE_KEY = "object";
    private static final String STRING_TYPE_KEY = "string";
    private static final String NUMBER_TYPE_KEY = "number";
    private static final String INTEGER_TYPE_KEY = "integer";
    private static final String BOOLEAN_TYPE_KEY = "boolean";

//    todo deal with arrays
    private static final String ARRAY_TYPE_KEY = "array";

    private static final String TAG_KEY = "tag";
    private static final String NAME_KEY = "name";
    private static final String TITLE_KEY = "title";
    private static final String MERGE_FIELDS_KEY = "merge_fields";


    private static final Map<String, DataSourceAttributeType> MAIL_CHIMP_TYPE_TO_ATTR_TYPE = ImmutableMap.of(
            STRING_TYPE_KEY, DataSourceAttributeType.STRING,
            NUMBER_TYPE_KEY, DataSourceAttributeType.DOUBLE,
            INTEGER_TYPE_KEY, DataSourceAttributeType.LONG,
            BOOLEAN_TYPE_KEY, DataSourceAttributeType.BOOLEAN
    );


    private static final String TYPE_KEY = "type";
    public static final String PROPERTIES_KEY = "properties";
    public static final String ADDITIONAL_PROPERTIES_KEY = "additionalProperties";

    @Value("classpath:mailchimp/members_schema_fields.json")
    private Resource file;

    public static List<SimpleDataSourceField> mailChimpCustomerAttributes;


    @PostConstruct
    public void init() throws IOException {
        String jsonStr = IOUtils.toString(file.getInputStream(), Charset.defaultCharset());
        TypeReference<Map<String, Object>> typeReference = new TypeReference<Map<String, Object>>() {
        };

        Map<String, Object> membersSchema = JsonDataConverter.getInstance().fromString(jsonStr, typeReference);
        mailChimpCustomerAttributes = objectToAttributes((Map<String, Object>) membersSchema.get(PROPERTIES_KEY), null);
    }

    private List<SimpleDataSourceField> objectToAttributes(Map<String, Object> membersSchema, String currentPath) {
        if (membersSchema == null) {
            return Collections.emptyList();
        }

        return membersSchema.entrySet().stream().flatMap((entry) -> {
            String fieldName = entry.getKey();
            if(EXCLUDE_FIELDS.contains(fieldName)) {
                return null;
            }
            Object value = entry.getValue();

            if (value instanceof Map) {
                Map<String, Object> fieldDefinition = (Map<String, Object>) value;

                String currentType = String.valueOf(fieldDefinition.get(TYPE_KEY));

                if (OBJECT_TYPE_KEY.equals(currentType)) {
                    Map<String, Object> innerObjectDefinition = (Map<String, Object>) membersSchema.get(fieldName);
                    Map<String, Object> innerObjectProperties = (Map<String, Object>) innerObjectDefinition.getOrDefault(PROPERTIES_KEY,
                            innerObjectDefinition.get(ADDITIONAL_PROPERTIES_KEY));

                    return objectToAttributes(innerObjectProperties, MapUtils.buildKeyName(currentPath, fieldName))
                            .stream();
                }

                return Stream.of(new SimpleDataSourceField(
                        MapUtils.buildKeyName(currentPath, fieldName),
                        String.valueOf(fieldDefinition.get(TITLE_KEY)),
                        MAIL_CHIMP_TYPE_TO_ATTR_TYPE.get(currentType)
                ));
            }

            return null;
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public static List<SimpleDataSourceField> getAllFields(Map<String, Object> mergeFields) {
        List<SimpleDataSourceField> mergeSimpleFields = mapMergeFields(mergeFields);
        mergeSimpleFields.addAll(mailChimpCustomerAttributes);
        return mergeSimpleFields;
    }


    public static List<SimpleDataSourceField> mapMergeFields(Map<String, Object> map) {
        List<Map<String, Object>> mergeFields = (List<Map<String, Object>>) map.getOrDefault(MERGE_FIELDS_KEY, Collections.emptyList());

        return mergeFields.stream().flatMap(mergeField -> {
            String fieldName = MapUtils.buildKeyName(MERGE_FIELDS_KEY, String.valueOf(mergeField.get(TAG_KEY)));
            String displayName = String.valueOf(mergeField.get(NAME_KEY));

            String type = String.valueOf(mergeField.get(TYPE_KEY));
            if(type == null) {
                return Stream.of();
            }

            switch (type) {
                case ADDRESS_TYPE_KEY:
                    return DEFAULT_TYPES_MAPPINGS.get(ADDRESS_TYPE_KEY).stream();
                case NUMBER_TYPE_KEY:
                    return Stream.of(new SimpleDataSourceField(fieldName, displayName, DataSourceAttributeType.DOUBLE));
                default:
                    return Stream.of(new SimpleDataSourceField(fieldName, displayName, DataSourceAttributeType.STRING));
            }
        }).collect(Collectors.toList());

    }

    private static final Map<String, List<SimpleDataSourceField>> DEFAULT_TYPES_MAPPINGS = new HashMap<String, List<SimpleDataSourceField>>(){{
        this.put(ADDRESS_TYPE_KEY, Lists.newArrayList(
                buildSimpleField(ADDRESS_TYPE_KEY, "addr1", "Address 1", DataSourceAttributeType.STRING),
                buildSimpleField(ADDRESS_TYPE_KEY, "addr2", "Address 2", DataSourceAttributeType.STRING),
                buildSimpleField(ADDRESS_TYPE_KEY, "city", "City", DataSourceAttributeType.STRING),
                buildSimpleField(ADDRESS_TYPE_KEY, "state", "State", DataSourceAttributeType.STRING),
                buildSimpleField(ADDRESS_TYPE_KEY, "zip", "Zip", DataSourceAttributeType.STRING),
                buildSimpleField(ADDRESS_TYPE_KEY, "county", "County", DataSourceAttributeType.STRING)
        ));
    }};


    private static SimpleDataSourceField buildSimpleField(String parentPath, String fieldName, String displayName, DataSourceAttributeType attributeType) {
        return new SimpleDataSourceField(MapUtils.buildKeyName(parentPath, fieldName), displayName, attributeType);
    }
}
