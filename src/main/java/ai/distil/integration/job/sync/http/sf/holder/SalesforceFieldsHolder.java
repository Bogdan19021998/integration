package ai.distil.integration.job.sync.http.sf.holder;

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

    private static Map<String, DataSourceAttributeType> DEFAULT_ATTRIBUTES_TYPE = new HashMap<String, DataSourceAttributeType>() {{
        this.put("xsd:string", DataSourceAttributeType.TEXT);

        this.put("xsd:boolean", DataSourceAttributeType.BOOLEAN);
        this.put("xsd:double", DataSourceAttributeType.DOUBLE);
        this.put("xsd:decimal", DataSourceAttributeType.DECIMAL);
        this.put("xsd:float", DataSourceAttributeType.FLOAT);

        this.put("xsd:byte", DataSourceAttributeType.INTEGER);
        this.put("xsd:int", DataSourceAttributeType.INTEGER);
        this.put("xsd:integer", DataSourceAttributeType.INTEGER);
        this.put("xsd:short", DataSourceAttributeType.INTEGER);
        this.put("xsd:signedInt", DataSourceAttributeType.INTEGER);
        this.put("xsd:unsignedShort", DataSourceAttributeType.INTEGER);

        this.put("tns:ID", DataSourceAttributeType.BIGINT);
        this.put("xsd:long", DataSourceAttributeType.BIGINT);
        this.put("xsd:unsignedInt", DataSourceAttributeType.BIGINT);

        this.put("xsd:date", DataSourceAttributeType.DATE);
        this.put("xsd:dateTime", DataSourceAttributeType.TIMESTAMP);
    }};

    private static Map<DataSourceSchemaAttributeTag, Set<String>> DEFAULT_ATTRIBUTES_TAGS = new HashMap<DataSourceSchemaAttributeTag, Set<String>>() {{
        this.put(DataSourceSchemaAttributeTag.CUSTOMER_EMAIL_ADDRESS, Sets.newHashSet("EMAIL", "EMAILADDRESS"));
        this.put(DataSourceSchemaAttributeTag.CUSTOMER_POSTCODE, Sets.newHashSet("POSTCODE", "ZIP"));
        this.put(DataSourceSchemaAttributeTag.CUSTOMER_FIRST_NAME, Sets.newHashSet("FNAME", "FIRSTNAME", "GIVENNAME"));
        this.put(DataSourceSchemaAttributeTag.CUSTOMER_LAST_NAME, Sets.newHashSet("LNAME", "LASTNAME", "SURNAME"));
        this.put(DataSourceSchemaAttributeTag.CUSTOMER_MOBILE_NUMBER, Sets.newHashSet("PHONE", "MOBILENUMBER", "TELEPHONE", "TELNUMBER", "PHONENUMBER"));
        this.put(DataSourceSchemaAttributeTag.CUSTOMER_COUNTRY_CODE, Sets.newHashSet("COUNTRY", "COUNTRYCODE"));
        this.put(DataSourceSchemaAttributeTag.PRIMARY_KEY, Sets.newHashSet("ID"));
    }};

    @Override
    public Set<String> getExcludeFields() {
        return Sets.newHashSet("Address");
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
