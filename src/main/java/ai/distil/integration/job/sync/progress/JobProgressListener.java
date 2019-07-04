package ai.distil.integration.job.sync.progress;

import java.io.Serializable;

public interface JobProgressListener<P> extends Serializable {
    void handle(P p);
}