package ai.distil.integration.utils;

import java.util.UUID;

public class NamingUtils {

    public static final int MAX_TABLE_NAME_LENGTH = 48;
    //    this is cassandra naming pattern
    private static final String NAMING_PATTERN = "[^a-zA-Z_0-9]+";

    private static final String TABLE_NAME_PREFIX = "t";
    private static final String COLUMN_NAME_PREFIX = "c";


    public static String sanitizeColumnName(String columnName) {
        return columnName.trim().toUpperCase().replaceAll("_", "");
    }

    public static String generateTableName(String sourceTable) {
        String tableNamePrefix = TABLE_NAME_PREFIX + "_";
        String hashCodeSuffix = "_" + Math.abs(UUID.randomUUID().hashCode());

        int currentNameLength = tableNamePrefix.length() + hashCodeSuffix.length();
        String tableNameSanitized = sourceTable.replaceAll(NAMING_PATTERN, "");

        return tableNamePrefix
                + tableNameSanitized.substring(0, Math.min(MAX_TABLE_NAME_LENGTH - currentNameLength, tableNameSanitized.length()))
                + hashCodeSuffix;
    }

    public static String generateColumnName(String sourceColumnName) {
        return COLUMN_NAME_PREFIX + sourceColumnName.replaceAll(NAMING_PATTERN, "") + "_" + Math.abs(UUID.randomUUID().hashCode());
    }

}
