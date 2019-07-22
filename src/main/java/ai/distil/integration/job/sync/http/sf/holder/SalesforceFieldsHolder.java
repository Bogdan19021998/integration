package ai.distil.integration.job.sync.http.sf.holder;

import ai.distil.integration.controller.dto.data.DatasetRow;
import ai.distil.integration.job.sync.http.IFieldsHolder;
import ai.distil.integration.job.sync.http.mailchimp.SimpleDataSourceField;
import ai.distil.integration.job.sync.http.sf.vo.SfField;
import ai.distil.integration.job.sync.jdbc.SimpleDataSourceDefinition;
import ai.distil.model.types.DataSourceAttributeType;
import ai.distil.model.types.DataSourceSchemaAttributeTag;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
public class SalesforceFieldsHolder implements IFieldsHolder<SfField> {
    private static Set<String> EXCLUDE_FIELDS = Sets.newHashSet("Address", "attributes", "MailingAddress");

    private static Map<String, DataSourceAttributeType> DEFAULT_ATTRIBUTES_TYPE = new HashMap<String, DataSourceAttributeType>() {{
        this.put("XSD:STRING", DataSourceAttributeType.TEXT);
        this.put("TNS:ID", DataSourceAttributeType.TEXT);

        this.put("XSD:BOOLEAN", DataSourceAttributeType.BOOLEAN);
        this.put("XSD:DOUBLE", DataSourceAttributeType.DOUBLE);
        this.put("XSD:DECIMAL", DataSourceAttributeType.DECIMAL);
        this.put("XSD:FLOAT", DataSourceAttributeType.FLOAT);

        this.put("XSD:BYTE", DataSourceAttributeType.INTEGER);
        this.put("XSD:INT", DataSourceAttributeType.INTEGER);
        this.put("XSD:INTEGER", DataSourceAttributeType.INTEGER);
        this.put("XSD:SHORT", DataSourceAttributeType.INTEGER);
        this.put("XSD:SIGNEDINT", DataSourceAttributeType.INTEGER);
        this.put("XSD:UNSIGNEDSHORT", DataSourceAttributeType.INTEGER);

        this.put("XSD:LONG", DataSourceAttributeType.BIGINT);
        this.put("XSD:UNSIGNEDINT", DataSourceAttributeType.BIGINT);

//      TODO think about it
        this.put("XSD:DATE", DataSourceAttributeType.TEXT);
        this.put("XSD:DATETIME", DataSourceAttributeType.TEXT);
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

    public DatasetRow transformRow(Map<String, Object> row) {
        DatasetRow.DatasetRowBuilder builder = new DatasetRow.DatasetRowBuilder(row.size());

        row.forEach((k, v) -> {
            if(!getExcludeFields().contains(k)) {
                builder.addValue(k, v);
            }
        });

        return builder.build();
    }

    @Override
    public Map<String, DataSourceAttributeType> getDataTypeMapping() {
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
}
