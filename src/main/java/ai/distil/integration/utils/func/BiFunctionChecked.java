package ai.distil.integration.utils.func;

@FunctionalInterface
public interface BiFunctionChecked<T, K, R> {
    R apply(T t, K k) throws Exception;
}
