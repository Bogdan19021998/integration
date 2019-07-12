package ai.distil.integration.utils;

public class NamingUtils {
    public static String sanitizeColumnName(String columnName) {
        return columnName.trim().toUpperCase().replaceAll("_", "");
    }
}
