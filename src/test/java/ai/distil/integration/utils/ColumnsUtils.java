package ai.distil.integration.utils;

import java.util.Arrays;

public class ColumnsUtils {

    private static final String DEFAULT_KEY_SEPARATOR = "_";

    // just remove the key part after the latest _
    public static String normalizeKeyForComparison(String key) {
        String[] keySplit = key.split(DEFAULT_KEY_SEPARATOR);

        if(keySplit.length == 1) {
            return key;
        }

        return String.join(DEFAULT_KEY_SEPARATOR, Arrays.copyOfRange(keySplit, 0, keySplit.length - 1));

    }

}
