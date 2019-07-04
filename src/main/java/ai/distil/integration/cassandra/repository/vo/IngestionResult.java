package ai.distil.integration.cassandra.repository.vo;

import com.datastax.driver.core.ResultSetFuture;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IngestionResult {
    private ResultSetFuture resultSetFuture;
    private IngestionStatus ingestionStatus;
    private String primaryKey;
}
