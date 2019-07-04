package ai.distil.integration.utils;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Slf4j
public class ListUtils {

    public static <K, V> Map<K, V> groupByWithOverwriteSilent(List<V> list, Function<V, K> groupingFunction) {
        return groupByWithOverwrite(list, groupingFunction, false);
    }

    public static <K, V> Map<K, V> groupByWithOverwrite(List<V> list, Function<V, K> groupingFunction, boolean throwIfNotUnique) {
        Map<K, V> r = new HashMap<>();
        list.forEach(l -> {
            K key = groupingFunction.apply(l);
            V value = r.get(key);

            if (throwIfNotUnique && value != null) {
                throw new IllegalStateException("Can't group by list, there is a duplicate value by key " + key);
            }
            r.put(key, l);
        });

        return r;
    }

    public static <K, V, T> Map<K, T> groupByWithOverwrite(List<V> list, Function<V, K> groupingFunction, Function<V, T> valueFunctino) {
        Map<K, T> r = new HashMap<>();
        list.forEach(l -> {
            K key = groupingFunction.apply(l);
            T value = valueFunctino.apply(l);

            r.put(key, value);
        });

        return r;
    }
}
