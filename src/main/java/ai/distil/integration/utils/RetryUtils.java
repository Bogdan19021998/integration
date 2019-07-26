package ai.distil.integration.utils;

import com.datastax.driver.core.exceptions.ReadTimeoutException;
import net.jodah.failsafe.Failsafe;
import net.jodah.failsafe.RetryPolicy;

import java.time.Duration;
import java.util.function.Supplier;

public class RetryUtils {

    public static <T> T defaultCassandraReadTimeoutRetry(Supplier<T> supplier) {
        RetryPolicy<Object> retryPolicy = new RetryPolicy<>()
                .handle(ReadTimeoutException.class)
                .withDelay(Duration.ofMillis(1500))
                .withMaxRetries(3);

        return Failsafe.with(retryPolicy).get(supplier::get);
    }

}
