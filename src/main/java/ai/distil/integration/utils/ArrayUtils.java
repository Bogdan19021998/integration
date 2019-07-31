package ai.distil.integration.utils;

import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
public class ArrayUtils {

    public static <T> Optional<T> get(int i, T ... t) {
        return i >= 0 && i < t.length ? Optional.ofNullable(t[i]) : Optional.empty();
    }
}
