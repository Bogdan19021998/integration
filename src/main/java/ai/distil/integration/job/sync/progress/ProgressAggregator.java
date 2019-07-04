package ai.distil.integration.job.sync.progress;

import ai.distil.integration.cassandra.repository.vo.IngestionResult;
import lombok.Getter;

import java.util.Date;


public class ProgressAggregator {
    @Getter
    private SyncProgressTrackingData syncTrackingData;

    public ProgressAggregator() {
        this.syncTrackingData = new SyncProgressTrackingData();
    }

    public void aggregate(IngestionResult ingestionResult) {
        switch (ingestionResult.getIngestionStatus()) {
            case CREATED:
                this.syncTrackingData.incrementCreatesCounter();
                break;
            case NOT_CHANGED:
                this.syncTrackingData.incrementNotChangedCounter();
                break;
            case UPDATED:
                this.syncTrackingData.incrementUpdatesCounter();
                break;
        }
    }

    public void setDeletedCount(long deletedCount) {
        syncTrackingData.setDeleted(deletedCount);
    }

    public void setCurrentRowsCount(long currentRowsCount) {
        syncTrackingData.setCurrentRowsCount(currentRowsCount);
    }

    public void setBeforeRowsCount(long beforeRowsCount) {
        syncTrackingData.setBeforeRowsCount(beforeRowsCount);
    }

    public void startTracking() {
        this.syncTrackingData.setStartedDate(new Date());
    }

    public void stopTracking() {
        this.syncTrackingData.setFinishedDate(new Date());
    }

}
