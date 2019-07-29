package ai.distil.integration.job.sync.progress;

import ai.distil.integration.cassandra.repository.vo.IngestionResult;
import ai.distil.integration.cassandra.repository.vo.IngestionStatus;
import lombok.Getter;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.Date;
import java.util.Set;

public class ProgressAggregator {
    @Getter
    private SyncProgressTrackingData syncTrackingData;

    @Getter
    private long consecutiveErrors = 0;

    public ProgressAggregator() {
        this.syncTrackingData = new SyncProgressTrackingData();
    }

    public void aggregate(IngestionResult ingestionResult, Set<String> existingPrimaryKeys) {
        if(ingestionResult.getIngestionStatus() != IngestionStatus.ERROR && existingPrimaryKeys.contains(ingestionResult.getPrimaryKey())) {
            this.syncTrackingData.incrementDuplicatesCounter();
            return;
        }

        switch (ingestionResult.getIngestionStatus()) {
            case CREATED:
                this.syncTrackingData.incrementCreatesCounter();
                consecutiveErrors = 0;
                break;
            case NOT_CHANGED:
                this.syncTrackingData.incrementNotChangedCounter();
                consecutiveErrors = 0;
                break;
            case UPDATED:
                this.syncTrackingData.incrementUpdatesCounter();
                consecutiveErrors = 0;
                break;
            case ERROR:
                this.syncTrackingData.incrementErrorsCount();
                consecutiveErrors++;
                break;
            default:
                throw new NotImplementedException();
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
