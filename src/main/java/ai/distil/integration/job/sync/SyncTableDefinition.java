package ai.distil.integration.job.sync;

import ai.distil.model.types.DataSourceSchemaAttributeTag;
import ai.distil.model.types.DataSourceType;
import com.google.common.collect.Sets;
import lombok.Getter;

import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ai.distil.integration.utils.NamingUtils.sanitizeColumnName;
import static ai.distil.model.types.DataSourceSchemaAttributeTag.*;

public enum SyncTableDefinition {

    CUSTOMER("CUSTOMER", "customers", DataSourceType.CUSTOMER,
            new HashMap<DataSourceSchemaAttributeTag, Set<String>>() {{
                put(CUSTOMER_EXTERNAL_ID, Sets.newHashSet("ID"));
                put(DataSourceSchemaAttributeTag.CUSTOMER_EMAIL_ADDRESS, Sets.newHashSet("EMAIL", "EMAILADDRESS"));
                put(DataSourceSchemaAttributeTag.CUSTOMER_POSTCODE, Sets.newHashSet("POSTCODE"));
                put(DataSourceSchemaAttributeTag.CUSTOMER_FIRST_NAME, Sets.newHashSet("FIRSTNAME", "GIVENNAME"));
                put(DataSourceSchemaAttributeTag.CUSTOMER_LAST_NAME, Sets.newHashSet("LASTNAME", "SURNAME"));
                put(DataSourceSchemaAttributeTag.CUSTOMER_MOBILE_NUMBER, Sets.newHashSet("MOBILENUMBER", "TELEPHONE", "TELNUMBER", "PHONENUMBER"));
                put(DataSourceSchemaAttributeTag.CUSTOMER_COUNTRY_CODE, Sets.newHashSet("EMAIL", "EMAILADDRESS", "COUNTRY", "COUNTRYCODE"));
            }}
    ),
    PRODUCT("PRODUCT", "products", DataSourceType.PRODUCT,
            new HashMap<DataSourceSchemaAttributeTag, Set<String>>() {{
                put(PRODUCT_EXTERNAL_ID, Sets.newHashSet("ID"));
                put(DataSourceSchemaAttributeTag.PRODUCT_NAME, Sets.newHashSet("NAME", "TITLE"));
                put(DataSourceSchemaAttributeTag.PRODUCT_SHOP_URL, Sets.newHashSet("URL", "PRODUCTURL", "LINK", "SHOPURL"));
                put(DataSourceSchemaAttributeTag.PRODUCT_IMAGE_URL, Sets.newHashSet("IMAGE", "IMAGEURL", "IMAGELINK"));
            }}),
    CONTENT("CONTENT", "content", DataSourceType.CONTENT,
            new HashMap<DataSourceSchemaAttributeTag, Set<String>>() {{
                put(CONTENT_EXTERNAL_ID, Sets.newHashSet("ID"));
                put(DataSourceSchemaAttributeTag.CONTENT_TITLE, Sets.newHashSet("NAME", "TITLE"));
                put(DataSourceSchemaAttributeTag.CONTENT_URL, Sets.newHashSet("URL", "CONTENTURL", "LINK"));
                put(DataSourceSchemaAttributeTag.CONTENT_IMAGE_URL, Sets.newHashSet("IMAGE", "IMAGEURL", "IMAGELINK"));
            }}),
    ORDER("ORDER", "orders", DataSourceType.ORDER,
            new HashMap<DataSourceSchemaAttributeTag, Set<String>>() {{
                put(ORDER_EXTERNAL_ID, Sets.newHashSet("ID"));
            }}),
    PURCHASE_HISTORY("PURCHASE_HISTORY", "orders", DataSourceType.ORDER,
            new HashMap<DataSourceSchemaAttributeTag, Set<String>>() {{
                put(ORDER_EXTERNAL_ID, Sets.newHashSet("ID"));
            }});

    private static final String DISTIL_MARKER = "DISTIL";

    @Getter
    private final String baseSourceTableName;
    @Getter
    private final String distilTableName;
    @Getter
    private final DataSourceType dataSourceType;

    private final Set<String> eligibleTablesNames;

    @Getter
    private final Map<DataSourceSchemaAttributeTag, Set<String>> attributeTags;

    SyncTableDefinition(String baseSourceTableName, String distilTableName, DataSourceType dataSourceType, Map<DataSourceSchemaAttributeTag, Set<String>> attributeTags) {
        this.baseSourceTableName = baseSourceTableName;
        this.distilTableName = distilTableName;
        this.attributeTags = attributeTags;
        this.dataSourceType = dataSourceType;

        this.eligibleTablesNames = Stream.of(
                buildSingularAndPluralNames("%s_%s", DISTIL_MARKER, baseSourceTableName),
                buildSingularAndPluralNames("%s%s", DISTIL_MARKER, baseSourceTableName),
                buildSingularAndPluralNames("%s-%s", DISTIL_MARKER, baseSourceTableName),
                buildSingularAndPluralNames("V_%s_%s", DISTIL_MARKER, baseSourceTableName),
                buildSingularAndPluralNames("V-%s-%s", DISTIL_MARKER, baseSourceTableName)
        ).flatMap(Collection::stream).collect(Collectors.toSet());
    }

    public static Optional<SyncTableDefinition> identifySyncTableDefinition(String tableName) {
        return Stream.of(SyncTableDefinition.values())
                .filter(tableDefinition -> tableDefinition.isTableNameFitNamingConvention(tableName))
                .findFirst();
    }

    public static boolean isTableEligibleForRun(String tableName) {
        return Stream.of(SyncTableDefinition.values()).anyMatch(tb -> tb.isTableNameFitNamingConvention(tableName));
    }

    public DataSourceSchemaAttributeTag tryToGetAttributeType(@NotNull String columnName) {
        String column = sanitizeColumnName(columnName);

        return this.getAttributeTags().entrySet().stream()
                .filter(entry -> entry.getValue().contains(column))
                .findFirst()
                .map(Map.Entry::getKey)
                .orElse(DataSourceSchemaAttributeTag.NONE);
    }

    public boolean isTableNameFitNamingConvention(String tableName) {
        return this.eligibleTablesNames.contains(tableName.trim().toUpperCase());
    }

    private Set<String> buildSingularAndPluralNames(String pattern, String... values) {
        return Sets.newHashSet(
                String.format(pattern, (Object[]) values),
                String.format(pattern, (Object[]) values) + "S"
        );
    }

}
