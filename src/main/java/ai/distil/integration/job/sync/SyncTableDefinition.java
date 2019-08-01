package ai.distil.integration.job.sync;

import ai.distil.api.internal.model.dto.DTODataSource;
import ai.distil.api.internal.model.dto.DTODataSourceAttribute;
import ai.distil.model.types.DataSourceAttributeType;
import ai.distil.model.types.DataSourceSchemaAttributeTag;
import ai.distil.model.types.DataSourceType;
import com.google.common.collect.Sets;
import lombok.*;

import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ai.distil.integration.utils.NamingUtils.sanitizeColumnName;
import static ai.distil.model.types.DataSourceAttributeType.*;
import static ai.distil.model.types.DataSourceSchemaAttributeTag.*;

public enum SyncTableDefinition {

    CUSTOMER("CUSTOMER", "customers", DataSourceType.CUSTOMER,
            new HashMap<DataSourceSchemaAttributeTag, FieldDefinition>() {{
                put(CUSTOMER_EXTERNAL_ID, FieldDefinition.builder()
                        .eligibleFieldNames(Sets.newHashSet("ID"))
                        .eligibleTypes(StaticTypesDefinition.ID_TYPES)
                        .mandatory(true)
                        .build());

                put(CUSTOMER_EMAIL_ADDRESS, FieldDefinition.builder()
                        .eligibleFieldNames(Sets.newHashSet("EMAIL", "EMAILADDRESS"))
                        .eligibleTypes(StaticTypesDefinition.STRING_TYPES)
                        .build());
                put(CUSTOMER_POSTCODE, FieldDefinition.builder()
                                .eligibleFieldNames(Sets.newHashSet("POSTCODE"))
                                .eligibleTypes(StaticTypesDefinition.STRING_TYPES)
                                .build());
                put(CUSTOMER_FIRST_NAME, FieldDefinition.builder()
                                .eligibleFieldNames(Sets.newHashSet("FIRSTNAME", "GIVENNAME"))
                                .eligibleTypes(StaticTypesDefinition.STRING_TYPES)
                                .build());

                put(CUSTOMER_LAST_NAME, FieldDefinition.builder()
                        .eligibleFieldNames(Sets.newHashSet("LASTNAME", "SURNAME"))
                        .eligibleTypes(StaticTypesDefinition.STRING_TYPES)
                        .build());
                put(CUSTOMER_MOBILE_NUMBER,
                        FieldDefinition.builder()
                                .eligibleFieldNames(Sets.newHashSet("MOBILENUMBER", "TELEPHONE", "TELNUMBER", "PHONENUMBER"))
                                .eligibleTypes(StaticTypesDefinition.STRING_TYPES)
                                .build());
                put(CUSTOMER_COUNTRY_CODE,
                        FieldDefinition.builder()
                                .eligibleFieldNames(Sets.newHashSet("COUNTRY", "COUNTRYCODE"))
                                .eligibleTypes(StaticTypesDefinition.STRING_TYPES)
                                .build());
            }}
    ),
    PRODUCT("PRODUCT", "products", DataSourceType.PRODUCT,
            new HashMap<DataSourceSchemaAttributeTag, FieldDefinition>() {{
                put(PRODUCT_EXTERNAL_ID,
                        FieldDefinition.builder()
                                .eligibleFieldNames(Sets.newHashSet("ID"))
                                .eligibleTypes(StaticTypesDefinition.ID_TYPES)
                                .mandatory(true)
                                .build());
                put(PRODUCT_NAME,
                        FieldDefinition.builder()
                                .eligibleFieldNames(Sets.newHashSet("NAME", "TITLE"))
                                .eligibleTypes(StaticTypesDefinition.STRING_TYPES)
                                .mandatory(true)
                                .build());

                put(PRODUCT_SHOP_URL,
                        FieldDefinition.builder()
                                .eligibleFieldNames(Sets.newHashSet("URL", "PRODUCTURL", "LINK", "SHOPURL"))
                                .eligibleTypes(StaticTypesDefinition.STRING_TYPES)
                                .mandatory(true)
                                .build());

                put(PRODUCT_IMAGE_URL,
                        FieldDefinition.builder()
                                .eligibleFieldNames(Sets.newHashSet("IMAGE", "IMAGEURL", "IMAGELINK"))
                                .eligibleTypes(StaticTypesDefinition.STRING_TYPES)
                                .build());
                put(PRODUCT_THUMBNAIL_URL,
                        FieldDefinition.builder()
                                .eligibleFieldNames(Sets.newHashSet("THUMBNAIL", "THUMBNAILURL", "THUMBNAILIMAGE", "THUMBNAILLINK"))
                                .eligibleTypes(StaticTypesDefinition.STRING_TYPES)
                                .build());

                put(PRODUCT_PRECIS,
                        FieldDefinition.builder()
                                .eligibleFieldNames(Sets.newHashSet("PRECIS"))
                                .eligibleTypes(StaticTypesDefinition.STRING_TYPES)
                                .build());

                put(PRODUCT_AVAILABLE,
                        FieldDefinition.builder()
                                .eligibleFieldNames(Sets.newHashSet("AVAILABLE", "ISAVAILABLE"))
                                .eligibleTypes(StaticTypesDefinition.BOOLEAN_TYPES)
                                .mandatory(true)
                                .build());

                put(PRODUCT_LIST_PRICE_EX_TAX,
                        FieldDefinition.builder()
                                .eligibleFieldNames(Sets.newHashSet("LISTPRICEEXTAX"))
                                .eligibleTypes(StaticTypesDefinition.NUMBER_TYPES)
                                .build());

                put(PRODUCT_LIST_PRICE_INC_TAX,
                        FieldDefinition.builder()
                                .eligibleFieldNames(Sets.newHashSet("LISTPRICEINCTAX"))
                                .eligibleTypes(StaticTypesDefinition.NUMBER_TYPES)
                                .build());

                put(PRODUCT_PRICE_BREAKS_DESCRIPTION,
                        FieldDefinition.builder()
                                .eligibleFieldNames(Sets.newHashSet("PRODUCTPRICEBREAKSDESCRIPTION", "PRICEBREAKSDESCRIPTION"))
                                .eligibleTypes(StaticTypesDefinition.NUMBER_TYPES)
                                .build());
            }}),
    CONTENT("CONTENT", "content", DataSourceType.CONTENT,
            new HashMap<DataSourceSchemaAttributeTag, FieldDefinition>() {{
                put(CONTENT_EXTERNAL_ID, FieldDefinition.builder()
                                .eligibleFieldNames(Sets.newHashSet("ID"))
                                .eligibleTypes(StaticTypesDefinition.ID_TYPES)
                                .mandatory(true)
                                .build());
                put(CONTENT_TITLE, FieldDefinition.builder()
                                .eligibleFieldNames(Sets.newHashSet("NAME", "TITLE"))
                                .eligibleTypes(StaticTypesDefinition.STRING_TYPES)
                                .mandatory(true)
                                .build());
                put(CONTENT_URL, FieldDefinition.builder()
                                .eligibleFieldNames(Sets.newHashSet("URL", "CONTENTURL", "LINK"))
                                .eligibleTypes(StaticTypesDefinition.STRING_TYPES)
                                .mandatory(true)
                                .build());

                put(CONTENT_IMAGE_URL, FieldDefinition.builder()
                                .eligibleFieldNames(Sets.newHashSet("IMAGE", "IMAGEURL", "IMAGELINK"))
                                .eligibleTypes(StaticTypesDefinition.STRING_TYPES)
                                .build());
            }}),
    ORDER("ORDER", "orders", DataSourceType.ORDER, StaticTypesDefinition.ORDER_TAGS_DEFINITION),
    PURCHASE_HISTORY("PURCHASE_HISTORY", "orders", DataSourceType.ORDER, StaticTypesDefinition.ORDER_TAGS_DEFINITION);


    private static final String DISTIL_MARKER = "DISTIL";

    @Getter
    private final String baseSourceTableName;
    @Getter
    private final String distilTableName;
    @Getter
    private final DataSourceType dataSourceType;
    @Getter
    private final Map<DataSourceSchemaAttributeTag, FieldDefinition> tagsDefinitions;
    private final Map<DataSourceSchemaAttributeTag, FieldDefinition> mangdatoryFields;



    SyncTableDefinition(String baseSourceTableName, String distilTableName, DataSourceType dataSourceType,
                        Map<DataSourceSchemaAttributeTag, FieldDefinition> attributeTags) {
        this.baseSourceTableName = baseSourceTableName;
        this.distilTableName = distilTableName;
        this.tagsDefinitions = attributeTags;
        this.dataSourceType = dataSourceType;
        this.mangdatoryFields = attributeTags.entrySet()
                .stream()
                .filter(m -> m.getValue().isMandatory())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public static Optional<SyncTableDefinition> identifySyncTableDefinition(String tableName) {
        return Stream.of(SyncTableDefinition.values())
                .filter(tableDefinition -> tableDefinition.isDataSourceAvailableByName(tableName))
                .findFirst();
    }

    public DataSourceSchemaAttributeTag tryToGetAttributeType(@NotNull String columnName) {
        String column = sanitizeColumnName(columnName);

        return this.getTagsDefinitions().entrySet().stream()
                .filter(entry -> entry.getValue().getEligibleFieldNames().contains(column))
                .findFirst()
                .map(Map.Entry::getKey)
                .orElse(NONE);
    }

    public boolean isDataSourceAvailableByName(String tableName) {
        return tableName.toUpperCase().contains(DISTIL_MARKER) &&
                tableName.toUpperCase().contains(this.baseSourceTableName.toUpperCase());
    }

    public boolean isDataSourceEligible(DTODataSource dataSource) {
        String tableName = dataSource.getSourceTableName();

        return isDataSourceAvailableByName(tableName) && allRequiredFieldsExists(dataSource.getAttributes());
    }


    private boolean allRequiredFieldsExists(List<DTODataSourceAttribute> attributes) {
        long countOfMandatoryFieldsInDataSource = this.mangdatoryFields.entrySet().stream().filter(definition ->
                attributes.stream()
                        .anyMatch(attr -> definition.getKey().equals(attr.getAttributeDataTag())
                                && definition.getValue().getEligibleTypes().contains(attr.getAttributeType()))).count();

        return countOfMandatoryFieldsInDataSource == this.mangdatoryFields.size();
    }

    public static Optional<SyncTableDefinition> defineSyncTableDefinition(DataSourceType dataSourceType) {
        return Stream.of(SyncTableDefinition.values())
                .filter(s -> s.getDataSourceType().equals(dataSourceType))
                .findFirst();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    private static final class FieldDefinition {
        private Set<String> eligibleFieldNames;
        private Set<DataSourceAttributeType> eligibleTypes;
        private boolean mandatory = false;
    }

    private static final class StaticTypesDefinition {
        private static final Set<DataSourceAttributeType> ID_TYPES = Sets.newHashSet(DECIMAL, BIGINT, INTEGER, LONG, UUID, STRING, TEXT);
        private static final Set<DataSourceAttributeType> STRING_TYPES = Sets.newHashSet(STRING, TEXT);
        private static final Set<DataSourceAttributeType> NUMBER_TYPES = Sets.newHashSet(BIGINT, INTEGER, LONG, DOUBLE, FLOAT);
        private static final Set<DataSourceAttributeType> BOOLEAN_TYPES = Sets.newHashSet(BOOLEAN);
        private static final Set<DataSourceAttributeType> DATE_TYPES = Sets.newHashSet(DATE, TIMESTAMP);

        //    it's must be in the separate class, otherwise we will have compilation error
        public static Map<DataSourceSchemaAttributeTag, FieldDefinition> ORDER_TAGS_DEFINITION = new HashMap<DataSourceSchemaAttributeTag, FieldDefinition>() {{
            put(ORDER_EXTERNAL_ID, FieldDefinition.builder()
                    .eligibleFieldNames(Sets.newHashSet("ID"))
                    .eligibleTypes(ID_TYPES)
                    .mandatory(true)
                    .build());

            put(CUSTOMER_EXTERNAL_ID, FieldDefinition.builder()
                    .eligibleFieldNames(Sets.newHashSet("CUSTOMERID"))
                    .eligibleTypes(ID_TYPES)
                    .mandatory(true)
                    .build());

            put(PRODUCT_EXTERNAL_ID, FieldDefinition.builder()
                    .eligibleFieldNames(Sets.newHashSet("PRODUCTID"))
                    .eligibleTypes(ID_TYPES)
                    .mandatory(true)
                    .build());

            put(ORDER_LINE_ITEM_QTY, FieldDefinition.builder()
                    .eligibleFieldNames(Sets.newHashSet("QUANTITY", "QTY", "LINEITEMQUANTITY", "LINEITEMQTY"))
                    .eligibleTypes(NUMBER_TYPES)
                    .mandatory(true)
                    .build());

            put(ORDER_LINE_ITEM_TIMESTAMP, FieldDefinition.builder()
                    .eligibleFieldNames(Sets.newHashSet("TIMESTAMP", "LINEITEMTIMESTAMP"))
                    .eligibleTypes(DATE_TYPES)
                    .mandatory(true)
                    .build());

            put(PRODUCT_LIST_PRICE_EX_TAX,
                    FieldDefinition.builder()
                            .eligibleFieldNames(Sets.newHashSet("LISTPRICEEXTAX"))
                            .eligibleTypes(NUMBER_TYPES)
                            .build());

            put(PRODUCT_LIST_PRICE_INC_TAX,
                    FieldDefinition.builder()
                            .eligibleFieldNames(Sets.newHashSet("LISTPRICEINCTAX"))
                            .eligibleTypes(NUMBER_TYPES)
                            .build());

            put(PRODUCT_PRICE_BREAKS_DESCRIPTION,
                    FieldDefinition.builder()
                            .eligibleFieldNames(Sets.newHashSet("PRODUCTPRICEBREAKSDESCRIPTION", "PRICEBREAKSDESCRIPTION"))
                            .eligibleTypes(NUMBER_TYPES)
                            .build());
        }};
    }

}
