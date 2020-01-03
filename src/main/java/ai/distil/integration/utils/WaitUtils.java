package ai.distil.integration.utils;

import java.util.function.Function;
import java.util.function.Supplier;

public class WaitUtils {

    public static <T> T wait(Supplier<T> dataFunc, Function<T, Boolean> checkForFinish, Integer waitUntilNextRun) {
        T v = dataFunc.get();
        for ( ; !checkForFinish.apply(v); v = dataFunc.get()) {
            try {
                Thread.sleep(waitUntilNextRun);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return v;
    }
}
