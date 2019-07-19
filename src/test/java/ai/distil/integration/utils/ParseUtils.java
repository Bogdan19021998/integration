package ai.distil.integration.utils;

import ai.distil.integration.job.sync.http.JsonDataConverter;
import com.fasterxml.jackson.core.type.TypeReference;

public class ParseUtils {

    public static <T> T parseJsonFile(String path, TypeReference<T> reference) {
        return JsonDataConverter.getInstance().parseStream(ParseUtils.class.getClassLoader().getResourceAsStream(path), reference);
    }
}
