package ai.distil.integration.utils;

import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

@Slf4j
public class ListUtils {

    public static <T> Optional<T> first(List<T> l) {
        if (l == null || l.size() == 0) {
            return Optional.empty();
        }
        return Optional.ofNullable(l.get(0));
    }

    public static <K, V> Map<K, V> groupByWithOverwriteSilent(Iterable<V> list, Function<V, K> groupingFunction) {
        return groupByWithOverwrite(list, groupingFunction, false);
    }

    public static <K, V> Map<K, V> groupByWithOverwrite(Iterable<V> list, Function<V, K> groupingFunction, boolean throwIfNotUnique) {
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

    public static <K, V, T> Map<K, T> groupByWithOverwrite(Iterable<V> list, Function<V, K> groupingFunction, Function<V, T> valueFunction) {
        Map<K, T> r = new HashMap<>();
        list.forEach(l -> {
            K key = groupingFunction.apply(l);
            T value = valueFunction.apply(l);

            r.put(key, value);
        });

        return r;
    }

    public static <K, V, T> Map<K, ? extends Collection<T>> groupBy(Iterable<V> list, Function<V, K> groupingFunction, Function<V, T> valueFunction, Supplier<Collection<T>> setGen) {
        Map<K, Collection<T>> r = new HashMap<>();

        list.forEach(l -> {
            K key = groupingFunction.apply(l);
            T value = valueFunction.apply(l);
            Collection<T> values = r.getOrDefault(key, setGen.get());
            values.add(value);

            r.put(key, values);
        });

        return r;
    }

    public static <K, V, T> Map<K, List<T>> groupBy(Iterable<V> list, Function<V, K> groupingFunction, Function<V, T> valueFunction) {
        return (Map<K, List<T>>) groupBy(list, groupingFunction, valueFunction, ArrayList::new);
    }

    public static <K, V, T> Map<K, Set<T>> groupByToLinkedSet(Iterable<V> list, Function<V, K> groupingFunction, Function<V, T> valueFunction) {
        return (Map<K, Set<T>>) groupBy(list, groupingFunction, valueFunction, LinkedHashSet::new);
    }
}
