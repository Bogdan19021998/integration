package ai.distil.integration.controller.dto.destination;

import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class SyncDestinationProgressData extends SyncProgressTrackingData {

    private static final Integer FAILED_EMAILS_THRESHOLD = 1000;

    /**
     * excluded usually means, that we not synced - due to lack of recommendations
     */
    private long excluded;

    private Set<String> failedSyncEmails = new HashSet<>();

    /**
     * @return means added or not, just in case someone worry
     * */
    public boolean addFailedEmails(Set<String> failedSyncEmails) {
        if(failedSyncEmails.size() < FAILED_EMAILS_THRESHOLD) {
            this.failedSyncEmails.addAll(failedSyncEmails);
            return true;
        }
        return false;
    }


    public void incrementExcludedCounter() {
        this.processed++;
        this.excluded++;
    }

}
