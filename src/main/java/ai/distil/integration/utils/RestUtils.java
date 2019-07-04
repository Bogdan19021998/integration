package ai.distil.integration.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;

import java.util.Optional;

@Slf4j
public class RestUtils {

    public static <T> Optional<T> getBody(ResponseEntity<T> r) {
        if (r == null) {
            return Optional.empty();
        }

        if (r.getStatusCode().is2xxSuccessful()) {
            return Optional.ofNullable(r.getBody());
        }
        return Optional.empty();
    }
}
