package ai.distil.integration.service;

import ai.distil.integration.controller.dto.BaseDestinationIntegrationRequest;
import ai.distil.integration.controller.dto.ScheduleConnectionSyncRequest;
import ai.distil.integration.controller.dto.ScheduleDatasourceSyncRequest;
import ai.distil.integration.job.JobDefinitionEnum;
import ai.distil.integration.mapper.JobMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class JobExecutionService {
    private final JobScheduler jobScheduler;
    private final JobMapper jobMapper;

    /**
     * Schedule a task and run it now
     * It's applicable for new synchronizations
     */
    public void scheduleDatasourceSyncTask(ScheduleDatasourceSyncRequest request) {
        jobScheduler.scheduleJob(JobDefinitionEnum.SYNC_DATASOURCE, jobMapper.mapSyncRequest(request));
        jobScheduler.startJobNow(JobDefinitionEnum.SYNC_DATASOURCE, jobMapper.mapSyncRequest(request));
    }

    public void runDatasourceSyncNow(ScheduleDatasourceSyncRequest request) {
        jobScheduler.scheduleOneTimeJobNow(JobDefinitionEnum.SYNC_DATASOURCE, jobMapper.mapSyncRequest(request));
    }

    public void scheduleConnectionSyncTask(ScheduleConnectionSyncRequest request) {
        jobScheduler.scheduleJob(JobDefinitionEnum.SYNC_CONNECTION, jobMapper.mapSyncConnectionRequest(request));
        jobScheduler.startJobNow(JobDefinitionEnum.SYNC_CONNECTION, jobMapper.mapSyncConnectionRequest(request));
    }

    public void runConnectionSyncNow(ScheduleConnectionSyncRequest request) {
        jobScheduler.scheduleOneTimeJobNow(JobDefinitionEnum.SYNC_CONNECTION, jobMapper.mapSyncConnectionRequest(request));
    }

    public void runDestinationSyncNow(BaseDestinationIntegrationRequest request) {
        jobScheduler.scheduleOneTimeJobNow(JobDefinitionEnum.SYNC_DESTINATION, jobMapper.mapDestinationSyncRequest(request));
    }

}
