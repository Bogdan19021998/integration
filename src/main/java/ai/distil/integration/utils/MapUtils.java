package ai.distil.integration.utils;

import io.netty.util.internal.StringUtil;

import java.util.*;
import java.util.function.Function;

public class MapUtils {

    public static final String DEFAULT_KEY_SEPARATOR = "_";

    public static Map<String, Object> flatten(Map<String, Object> map, Map<String, Function<?, Map<String, Object>>> customFieldsMapper) {
        return flatten(map, customFieldsMapper, Collections.emptyMap());
    }

    public static Map<String, Object> flatten(Map<String, Object> map,
                                              Map<String, Function<?, Map<String, Object>>> customFieldsMapper,
                                              Map<String, Function<Iterable<Map<String, Object>>, Map<String, Object>>> customArrayMapping) {
        return flatten(map, customFieldsMapper, customArrayMapping, null);
    }

    @SuppressWarnings("all")
    private static Map<String, Object> flatten(Map<String, Object> map,
                                               Map<String, Function<?, Map<String, Object>>> customFieldsMapper,
                                               Map<String, Function<Iterable<Map<String, Object>>, Map<String, Object>>> customArrayMapping,
                                               String currentPath) {

        return map.entrySet().stream().map((entry) -> {
            String key = entry.getKey();
            Object value = entry.getValue();

            Function<Object, Map<String, Object>> customMapper = (Function<Object, Map<String, Object>>) customFieldsMapper.get(key);

            if(customMapper != null) {
                return customMapper.apply(value);
            }

            if (value instanceof Map) {
                return flatten((Map) value, customFieldsMapper, customArrayMapping, buildKeyName(currentPath, key));
            } else if (value instanceof Iterable) {
                return Optional.ofNullable(customArrayMapping.get(key))
                        .map(transformFunction -> flatten(transformFunction.apply((Iterable<Map<String, Object>>) value), customFieldsMapper, customArrayMapping, buildKeyName(currentPath, key)))
                        .orElse(null);
            } else {
                return Collections.unmodifiableMap(Collections.singletonMap(buildKeyName(currentPath, key), value));
            }
        }).filter(Objects::nonNull).reduce(new HashMap<>(), (acc, m) -> {
            m.forEach(acc::put);
            return acc;
        });
    }

    public static String buildKeyName(String currentPath, String key) {
        if (StringUtil.isNullOrEmpty(currentPath)) {
            return key;
        }
        return currentPath + DEFAULT_KEY_SEPARATOR + key;
    }


}
