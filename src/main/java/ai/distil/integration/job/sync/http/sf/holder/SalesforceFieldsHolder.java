package ai.distil.integration.job.sync.http.sf.holder;

import ai.distil.integration.job.sync.http.IFieldsHolder;
import ai.distil.integration.job.sync.http.mailchimp.SimpleDataSourceField;
import ai.distil.integration.job.sync.http.sf.vo.SfField;
import ai.distil.integration.job.sync.jdbc.SimpleDataSourceDefinition;
import ai.distil.model.types.CassandraDataSourceAttributeType;
import ai.distil.model.types.DataSourceSchemaAttributeTag;
import com.datastax.driver.core.LocalDate;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Function;

@Slf4j
@Component
public class SalesforceFieldsHolder implements IFieldsHolder<SfField> {
    private static Set<String> EXCLUDE_FIELDS = Sets.newHashSet("Address", "attributes", "MailingAddress");

    private static Map<String, CassandraDataSourceAttributeType> DEFAULT_ATTRIBUTES_TYPE = new HashMap<String, CassandraDataSourceAttributeType>() {{
        this.put("XSD:STRING", CassandraDataSourceAttributeType.TEXT);
        this.put("TNS:ID", CassandraDataSourceAttributeType.TEXT);

        this.put("XSD:BOOLEAN", CassandraDataSourceAttributeType.BOOLEAN);
        this.put("XSD:DOUBLE", CassandraDataSourceAttributeType.DOUBLE);
        this.put("XSD:DECIMAL", CassandraDataSourceAttributeType.DECIMAL);
        this.put("XSD:FLOAT", CassandraDataSourceAttributeType.FLOAT);

        this.put("XSD:BYTE", CassandraDataSourceAttributeType.INTEGER);
        this.put("XSD:INT", CassandraDataSourceAttributeType.INTEGER);
        this.put("XSD:INTEGER", CassandraDataSourceAttributeType.INTEGER);
        this.put("XSD:SHORT", CassandraDataSourceAttributeType.INTEGER);
        this.put("XSD:SIGNEDINT", CassandraDataSourceAttributeType.INTEGER);
        this.put("XSD:UNSIGNEDSHORT", CassandraDataSourceAttributeType.INTEGER);

        this.put("XSD:LONG", CassandraDataSourceAttributeType.BIGINT);
        this.put("XSD:UNSIGNEDINT", CassandraDataSourceAttributeType.BIGINT);

        this.put("XSD:DATE", CassandraDataSourceAttributeType.DATE);
        this.put("XSD:DATETIME", CassandraDataSourceAttributeType.TIMESTAMP);
    }};

    private final Map<CassandraDataSourceAttributeType, Function<?, ?>> CUSTOM_VALUES_MAPPERS =
            new HashMap<CassandraDataSourceAttributeType, Function<?, ?>>() {{
                this.put(CassandraDataSourceAttributeType.DATE, value -> Optional.ofNullable((dateFormatter("yyyy-MM-dd").apply(value)))
                        .map(date -> LocalDate.fromMillisSinceEpoch(date.getTime())).orElse(null));
                this.put(CassandraDataSourceAttributeType.TIMESTAMP, dateFormatter("yyyy-MM-dd'T'HH:mm:ss.SSSZ"));
            }};



    private static Map<DataSourceSchemaAttributeTag, Set<String>> DEFAULT_ATTRIBUTES_TAGS = new HashMap<DataSourceSchemaAttributeTag, Set<String>>() {{
        this.put(DataSourceSchemaAttributeTag.CUSTOMER_EMAIL_ADDRESS, Sets.newHashSet("EMAIL", "EMAILADDRESS"));
        this.put(DataSourceSchemaAttributeTag.CUSTOMER_POSTCODE, Sets.newHashSet("POSTCODE", "ZIP"));
        this.put(DataSourceSchemaAttributeTag.CUSTOMER_FIRST_NAME, Sets.newHashSet("FNAME", "FIRSTNAME", "GIVENNAME"));
        this.put(DataSourceSchemaAttributeTag.CUSTOMER_LAST_NAME, Sets.newHashSet("LNAME", "LASTNAME", "SURNAME"));
        this.put(DataSourceSchemaAttributeTag.CUSTOMER_MOBILE_NUMBER, Sets.newHashSet("PHONE", "MOBILENUMBER", "TELEPHONE", "TELNUMBER", "PHONENUMBER"));
        this.put(DataSourceSchemaAttributeTag.CUSTOMER_COUNTRY_CODE, Sets.newHashSet("COUNTRY", "COUNTRYCODE"));
        this.put(DataSourceSchemaAttributeTag.CUSTOMER_EXTERNAL_ID, Sets.newHashSet("ID"));
    }};

    @Override
    public Set<String> getExcludeFields() {
        return EXCLUDE_FIELDS;
    }

    @Override
    public Map<String, CassandraDataSourceAttributeType> getDataTypeMapping() {
        return DEFAULT_ATTRIBUTES_TYPE;
    }

    @Override
    public Map<DataSourceSchemaAttributeTag, Set<String>> getAttributesTagsMappingByName() {
        return DEFAULT_ATTRIBUTES_TAGS;
    }

    @Override
    public List<SimpleDataSourceField> getStaticDataSourceFields() {
        return Collections.emptyList();
    }

    @Override
    public List<SimpleDataSourceField> getDynamicDataSourceField(SfField fieldsDefinition) {
        return Collections.singletonList(buildSimpleField(null, fieldsDefinition.getName(), fieldsDefinition.getLabel(), defineType(fieldsDefinition.getSoapType())));
    }

//  think about generalizing this, but I believe we will avoid this in futures
    public List<SimpleDataSourceDefinition> getPredefinedDataSources() {
        return Lists.newArrayList(
                new SimpleDataSourceDefinition(null, "Contact", null, null),
                new SimpleDataSourceDefinition(null, "Lead", null, null));
    }

    @Override
    public Map<CassandraDataSourceAttributeType, Function<?, ?>> getCustomTypeConverters() {
        return CUSTOM_VALUES_MAPPERS;
    }
}
