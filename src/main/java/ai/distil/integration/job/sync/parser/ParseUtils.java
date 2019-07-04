package ai.distil.integration.job.sync.parser;

public class ParseUtils {

    private static final String DEFAULT_ALIAS_PREFIX = "c";

    //  call intern on alias for adding string value to the string pool
    public static String buildAlias(Integer position) {
        return (DEFAULT_ALIAS_PREFIX + position).intern();
    }
}
