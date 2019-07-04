package ai.distil.integration.utils;

public class DateUtils {
    public static java.sql.Date toSqlDate(java.util.Date date) {
        if (date == null) {
            return null;
        }

        return new java.sql.Date(date.getTime());
    }
}
