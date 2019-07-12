package ai.distil.integration.controller;

import ai.distil.integration.controller.dto.ScheduleDatasourceSyncRequest;
import ai.distil.integration.controller.proxy.SyncJobProxy;
import ai.distil.integration.service.JobExecutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sync")
@RequiredArgsConstructor
public class SyncJobController implements SyncJobProxy {

    private final JobExecutionService jobExecutionService;

    @Override
    public void scheduleJob(ScheduleDatasourceSyncRequest request) {
        jobExecutionService.scheduleSyncTask(request);
    }

    @Override
    public void runSyncNow(ScheduleDatasourceSyncRequest request) {
        jobExecutionService.runNow(request);
    }

}
