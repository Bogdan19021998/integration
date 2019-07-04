package ai.distil.integration.utils;

public class StringUtils {

    public static final String EMPTY_STRING = "";

    public static Boolean equals(String str1, String str2) {
        if (str1 == null || str2 == null) {
            return false;
        }
        return str1.equals(str2);
    }

    public static String trimAndLowercase(String str) {
        if (str == null || EMPTY_STRING.equals(str)) {
            return null;
        }
        return str.trim().toLowerCase();
    }
}
