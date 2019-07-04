package ai.distil.integration.job.sync.progress;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SyncProgressTrackingData {
    private long currentTrackingTime;
    private long processed;
    private long created;
    private long updated;
    private long deleted;
    private long notChanged;
    private long currentRowsCount;
    private long beforeRowsCount;
    private Date startedDate;
    private Date finishedDate;

    public void incrementCreatesCounter() {
        this.processed++;
        this.created++;
    }

    public void incrementUpdatesCounter() {
        this.processed++;
        this.updated++;
    }

    public void incrementNotChangedCounter() {
        this.processed++;
        this.notChanged++;
    }

    @JsonIgnore
    public long getTaskDurationInSeconds() {
        return Math.round((this.finishedDate.getTime() - this.startedDate.getTime()) / 1000.);
    }

}
