package ai.distil.integration.utils.func;

@FunctionalInterface
public interface FunctionChecked<T, R> {
    R apply(T t) throws Exception;
}
