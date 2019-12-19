package ai.distil.integration.job.sync.progress;

import ai.distil.integration.controller.dto.destination.SyncProgressTrackingData;
import ai.distil.integration.domain.repository.SyncProgressTrackingDataRepository;
import ai.distil.integration.mapper.JobMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SimpleSyncProgressListener implements JobProgressListener<SyncProgressTrackingData> {

    private final SyncProgressTrackingDataRepository trackingDataRepository;
    private final JobMapper jobMapper;

    @Override
    public void handle(SyncProgressTrackingData progress) {
        trackingDataRepository.save(jobMapper.mapTrackingData(progress));
    }

}
