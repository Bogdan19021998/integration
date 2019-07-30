package ai.distil.integration.controller;

import ai.distil.integration.controller.dto.ScheduleConnectionSyncRequest;
import ai.distil.integration.controller.dto.ScheduleDatasourceSyncRequest;
import ai.distil.integration.controller.proxy.SyncJobProxy;
import ai.distil.integration.service.JobExecutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/sync")
@RequiredArgsConstructor
public class SyncJobController implements SyncJobProxy {

    private final JobExecutionService jobExecutionService;

    @Override
    public void scheduleSyncDatasourceJob(@Valid ScheduleDatasourceSyncRequest request) {
        jobExecutionService.scheduleDatasourceSyncTask(request);
    }

    @Override
    public void runSyncDatasourceNow(@Valid ScheduleDatasourceSyncRequest request) {
        jobExecutionService.runDatasourceSyncNow(request);
    }

    @Override
    public void scheduleConnectionJob(@Valid ScheduleConnectionSyncRequest request) {
        jobExecutionService.scheduleConnectionSyncTask(request);
    }

    @Override
    public void runSyncConnectionNow(@Valid ScheduleConnectionSyncRequest request) {
        jobExecutionService.runConnectionSyncNow(request);
    }

}
