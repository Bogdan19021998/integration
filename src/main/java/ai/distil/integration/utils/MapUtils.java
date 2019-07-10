package ai.distil.integration.utils;

import com.google.common.collect.ImmutableMap;
import io.netty.util.internal.StringUtil;

import java.util.*;
import java.util.function.Function;

public class MapUtils {

    public static final String DEFAULT_KEY_SEPARATOR = "_";

    public static Map<String, Object> flatten(Map<String, Object> map) {
        return flatten(map, Collections.emptyMap());
    }

    public static Map<String, Object> flatten(Map<String, Object> map, Map<String, Function<Iterable<Map<String, Object>>, Map<String, Object>>> customArrayMapping) {
        return flatten(map, customArrayMapping, null);
    }

    @SuppressWarnings("all")
    private static Map<String, Object> flatten(Map<String, Object> map, Map<String, Function<Iterable<Map<String, Object>>, Map<String, Object>>> customArrayMapping, String currentPath) {

        return map.entrySet().stream().map((entry) -> {
            String key = entry.getKey();
            Object value = entry.getValue();

            if (value instanceof Map) {
                return flatten((Map) value, customArrayMapping, buildKeyName(currentPath, key));
            } else if (value instanceof Iterable) {
                return Optional.ofNullable(customArrayMapping.get(key))
                        .map(transformFunction -> flatten(transformFunction.apply((Iterable<Map<String, Object>>) value), customArrayMapping, buildKeyName(currentPath, key)))
                        .orElse(null);
            } else {
                return ImmutableMap.of(buildKeyName(currentPath, key), value);
            }
        }).filter(Objects::nonNull).reduce(new HashMap<>(), (acc, m) -> {
            m.forEach(acc::put);
            return acc;
        });
    }

    private static String buildKeyName(String currentPath, String key) {
        if (StringUtil.isNullOrEmpty(currentPath)) {
            return key;
        }
        return currentPath + DEFAULT_KEY_SEPARATOR + key;
    }


}
