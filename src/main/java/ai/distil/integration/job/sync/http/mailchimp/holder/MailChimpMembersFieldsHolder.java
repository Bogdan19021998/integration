package ai.distil.integration.job.sync.http.mailchimp.holder;

import ai.distil.integration.job.sync.http.IFieldsHolder;
import ai.distil.integration.job.sync.http.JsonDataConverter;
import ai.distil.integration.job.sync.http.mailchimp.SimpleDataSourceField;
import ai.distil.model.types.CassandraDataSourceAttributeType;
import ai.distil.model.types.DataSourceSchemaAttributeTag;
import com.datastax.driver.core.LocalDate;
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
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Component
public class MailChimpMembersFieldsHolder implements IFieldsHolder<Map<String, Object>> {

    private static final Set<String> EXCLUDE_FIELDS = Sets.newHashSet("_links", "merge_fields", "marketing_permissions", "tags");
    private static final String NUMBER_TYPE_KEY = "number";

//    todo deal with arrays
    private static final String ARRAY_TYPE_KEY = "array";
    private static final String DATE_TIME_FORMAT_KEY = "date-time";

    private static final String ADDRESS_TYPE_KEY = "address";
    public static final String TAG_KEY = "tag";
    public static final String NAME_KEY = "name";
    public static final String MERGE_ID_KEY = "merge_id";
    public static final String MERGE_FIELDS_KEY = "merge_fields";

    private static final String TYPE_KEY = "type";
    private static final String FORMAT_KEY = "format";
    private static final String OBJECT_KEY = "object";
    private static final String DISPLAY_NAME_KEY = "title";
    private static final String PROPERTIES_KEY = "properties";
    private static final String TIMESTAMP_TYPE = "TIMESTAMP";

    private static final Set<String> PROPERTIES_KEYS = Sets.newHashSet("properties", "additionalProperties");

    private static final Map<String, CassandraDataSourceAttributeType> MAIL_CHIMP_TYPE_TO_ATTR_TYPE = ImmutableMap.of(
            "STRING", CassandraDataSourceAttributeType.STRING,
            "NUMBER", CassandraDataSourceAttributeType.DOUBLE,
            "INTEGER", CassandraDataSourceAttributeType.BIGINT,
            "BOOLEAN", CassandraDataSourceAttributeType.BOOLEAN,
            TIMESTAMP_TYPE, CassandraDataSourceAttributeType.TIMESTAMP
    );

    private static final Map<DataSourceSchemaAttributeTag, Set<String>> TAGS_MAPPING = new HashMap<DataSourceSchemaAttributeTag, Set<String>>() {{
        this.put(DataSourceSchemaAttributeTag.CUSTOMER_EXTERNAL_ID, Sets.newHashSet("ID"));
        this.put(DataSourceSchemaAttributeTag.CUSTOMER_EMAIL_ADDRESS, Sets.newHashSet("EMAILADDRESS"));
        this.put(DataSourceSchemaAttributeTag.CUSTOMER_FIRST_NAME, Sets.newHashSet("FNAME", "FIRSTNAME", "GIVENNAME"));
        this.put(DataSourceSchemaAttributeTag.CUSTOMER_LAST_NAME, Sets.newHashSet("LNAME", "SECONDNAME", "SURNAME"));
        this.put(DataSourceSchemaAttributeTag.CUSTOMER_MOBILE_NUMBER, Sets.newHashSet("PHONE", "MOBILENUMBER", "TELEPHONE", "TELNUMBER", "PHONENUMBER"));
        this.put(DataSourceSchemaAttributeTag.CUSTOMER_COUNTRY_CODE, Sets.newHashSet("COUNTRY", "COUNTRYCODE"));
        this.put(DataSourceSchemaAttributeTag.CUSTOMER_POSTCODE, Sets.newHashSet("POSTCODE", "ZIP"));

    }};

    private final List<SimpleDataSourceField> DEFAULT_ADDRESS_MAPPINGS = Lists.newArrayList(
            buildSimpleField(ADDRESS_TYPE_KEY.toUpperCase(), "addr1", "Address 1", CassandraDataSourceAttributeType.STRING),
            buildSimpleField(ADDRESS_TYPE_KEY.toUpperCase(), "addr2", "Address 2", CassandraDataSourceAttributeType.STRING),
            buildSimpleField(ADDRESS_TYPE_KEY.toUpperCase(), "city", "City", CassandraDataSourceAttributeType.STRING),
            buildSimpleField(ADDRESS_TYPE_KEY.toUpperCase(), "state", "State", CassandraDataSourceAttributeType.STRING),
            buildSimpleField(ADDRESS_TYPE_KEY.toUpperCase(), "zip", "Zip", CassandraDataSourceAttributeType.STRING),
            buildSimpleField(ADDRESS_TYPE_KEY.toUpperCase(), "county", "County", CassandraDataSourceAttributeType.STRING)
    );


    private final Map<String, Function<Map<String, Object>, List<SimpleDataSourceField>>> CUSTOM_FIELDS_MAPPING = new HashMap<String, Function<Map<String, Object>, List<SimpleDataSourceField>>>() {{
        this.put(ADDRESS_TYPE_KEY, (row) -> DEFAULT_ADDRESS_MAPPINGS);
        this.put(NUMBER_TYPE_KEY, (row) -> Collections.singletonList(buildDataSourceFieldFromMergeObject(row, CassandraDataSourceAttributeType.DOUBLE)));
        this.put(DEFAULT_TYPE_KEY, row -> Collections.singletonList(buildDataSourceFieldFromMergeObject(row, CassandraDataSourceAttributeType.STRING)));
    }};

    private final Map<CassandraDataSourceAttributeType, Function<?, ?>> CUSTOM_VALUES_MAPPERS = new HashMap<CassandraDataSourceAttributeType, Function<?, ?>>() {{
                this.put(CassandraDataSourceAttributeType.DATE, value -> LocalDate.fromMillisSinceEpoch(dateFormatter("yyyy-MM-dd").apply(value).getTime()));
                this.put(CassandraDataSourceAttributeType.TIMESTAMP, dateFormatter("yyyy-MM-dd'T'HH:mm:ssXXX"));
            }};

    private SimpleDataSourceField buildDataSourceFieldFromMergeObject(Map<String, Object> row, CassandraDataSourceAttributeType attributeType) {
        String displayName = String.valueOf(row.get(NAME_KEY));
        String fieldName = String.valueOf(row.get(TAG_KEY));

        return buildSimpleField(MERGE_FIELDS_KEY, fieldName, displayName, attributeType);
    }

    @Value("classpath:mailchimp/members_schema_fields.json")
    private Resource file;

    public static List<SimpleDataSourceField> DEFAULT_KEYS;


    @PostConstruct
    public void init() throws IOException {
        String jsonStr = IOUtils.toString(file.getInputStream(), Charset.defaultCharset());
        TypeReference<Map<String, Object>> typeReference = new TypeReference<Map<String, Object>>() {};

        Map<String, Object> membersSchema = JsonDataConverter.getInstance().fromString(jsonStr, typeReference);
        DEFAULT_KEYS = objectToAttributes((Map<String, Object>) membersSchema.get(PROPERTIES_KEY), null);
    }

    @Override
    public Set<String> getExcludeFields() {
        return EXCLUDE_FIELDS;
    }

    @Override
    public Map<String, CassandraDataSourceAttributeType> getDataTypeMapping() {
        return MAIL_CHIMP_TYPE_TO_ATTR_TYPE;
    }

    @Override
    public Map<DataSourceSchemaAttributeTag, Set<String>> getAttributesTagsMappingByName() {
        return TAGS_MAPPING;
    }

    @Override
    public List<SimpleDataSourceField> getStaticDataSourceFields() {
        return DEFAULT_KEYS;
    }

    @Override
    public List<SimpleDataSourceField> getDynamicDataSourceField(Map<String, Object> fieldsDefinition) {
        List<Map<String, Object>> rows = (List<Map<String, Object>>) fieldsDefinition.getOrDefault(MERGE_FIELDS_KEY, Collections.emptyList());

        return rows.stream().flatMap(row -> {
            String type = String.valueOf(row.get(TYPE_KEY));

            return CUSTOM_FIELDS_MAPPING.getOrDefault(type, CUSTOM_FIELDS_MAPPING.get(DEFAULT_TYPE_KEY))
                    .apply(row)
                    .stream();
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    @Override
    public Map<CassandraDataSourceAttributeType, Function<?, ?>> getCustomTypeConverters() {
        return CUSTOM_VALUES_MAPPERS;
    }

    private Map<String, String> FORMAT_MAPPING = new HashMap<String, String>(){{
        this.put(DATE_TIME_FORMAT_KEY, TIMESTAMP_TYPE);
    }};

    private List<SimpleDataSourceField> objectToAttributes(Map<String, Object> membersSchema, String currentPath) {
        if (membersSchema == null) {
            return Collections.emptyList();
        }

        return membersSchema.entrySet().stream().flatMap((entry) -> {
            String fieldName = entry.getKey();
            if(getExcludeFields().contains(fieldName)) {
                return null;
            }
            Object value = entry.getValue();

            if (value instanceof Map) {
                Map<String, Object> fieldDefinition = (Map<String, Object>) value;

                String currentFormat = String.valueOf(fieldDefinition.get(FORMAT_KEY));
                String currentType = FORMAT_MAPPING.getOrDefault(currentFormat, String.valueOf(fieldDefinition.get(TYPE_KEY)));

                if (OBJECT_KEY.equals(currentType)) {
                    Map<String, Object> innerObjectDefinition = (Map<String, Object>) membersSchema.get(fieldName);
                    Map<String, Object> innerObjectProperties = PROPERTIES_KEYS.stream().filter(innerObjectDefinition::containsKey)
                            .findFirst()
                            .map(key -> ((Map<String, Object>) innerObjectDefinition.get(key)))
                            .orElse(null);

                    return objectToAttributes(innerObjectProperties, buildFieldName(currentPath, fieldName))
                            .stream();
                }

                return Stream.of(new SimpleDataSourceField(
                        buildFieldName(currentPath, fieldName),
                        String.valueOf(fieldDefinition.get(DISPLAY_NAME_KEY)),
                        defineType(currentType),
                        tryToDefineTag(fieldName, currentType)
                ));
            }

            return null;
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

}
