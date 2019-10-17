package ai.distil.integration.job.sync.progress;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class SyncDestinationProgressData extends SyncProgressTrackingData {

    /**
     * excluded usually means, that we not synced - due to lack of recommendations
     */
    private long excluded;

    public void incrementExcludersCounter() {
        this.processed++;
        this.excluded++;
    }

}
