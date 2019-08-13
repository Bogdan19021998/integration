package ai.distil.integration.job.sync.progress;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SyncProgressTrackingData {
    private long currentTrackingTime;
    private long processed;
    private long created;
    private long updated;
    private long deleted;
    private long duplicates;
    private long errorsCount;
    private long notChanged;
    private long currentRowsCount;
    private long beforeRowsCount;
    private Date startedDate;
    private Date finishedDate;

    private Map<Long, Long> notNullAttributeValues = new HashMap<>();

    public void incrementCreatesCounter() {
        this.processed++;
        this.created++;
    }

    public void incrementDuplicatesCounter() {
        this.processed++;
        this.duplicates++;
    }

    public void incrementUpdatesCounter() {
        this.processed++;
        this.updated++;
    }

    public void incrementNotChangedCounter() {
        this.processed++;
        this.notChanged++;
    }

    public void incrementErrorsCount() {
        this.processed++;
        this.errorsCount++;
    }

    public void aggregateAttributeValues(Set<Long> notNullAttributesIds) {
        if(notNullAttributesIds == null) {
            return;
        }

        notNullAttributesIds.forEach(attributeId -> {
            Long currentNotNullRowsCount = this.notNullAttributeValues.getOrDefault(attributeId, 0L) + 1L;
            this.notNullAttributeValues.put(attributeId, currentNotNullRowsCount);
        });
    }

    @JsonIgnore
    public long getTaskDurationInSeconds() {
        return Math.round((this.finishedDate.getTime() - this.startedDate.getTime()) / 1000.);
    }

}
