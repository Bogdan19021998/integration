package ai.distil.integration.utils;

import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Slf4j
public class ConcurrentUtils {

//    todo add retries
    public static  <T> List<T> wait(List<CompletableFuture<T>> futures) {
        return futures.stream().map(f -> {
            try {
                return f.get();
            } catch (InterruptedException | ExecutionException e) {
                log.error("Can't finish futures execution", e);
            }
            return null;
        }).collect(Collectors.toList());
    }
}
