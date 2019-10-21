package ai.distil.integration.controller;

import ai.distil.integration.controller.dto.BaseDestinationIntegrationRequest;
import ai.distil.integration.controller.dto.ScheduleConnectionSyncRequest;
import ai.distil.integration.controller.dto.ScheduleDatasourceSyncRequest;
import ai.distil.integration.controller.proxy.SyncJobProxy;
import ai.distil.integration.service.JobExecutionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/sync")
@RequiredArgsConstructor
public class SyncJobController implements SyncJobProxy {

    private final JobExecutionService jobExecutionService;

    @Override
    public void scheduleSyncDatasourceJob(@Valid ScheduleDatasourceSyncRequest request) {
        log.debug("Scheduling datasource job sync {}", request);
        jobExecutionService.scheduleDatasourceSyncTask(request);
    }

    @Override
    public void runSyncDatasourceNow(@Valid ScheduleDatasourceSyncRequest request) {
        log.debug("Run datasource job sync now {}", request);
        jobExecutionService.runDatasourceSyncNow(request);
    }

    @Override
    public void scheduleConnectionJob(@Valid ScheduleConnectionSyncRequest request) {
        log.debug("Scheduling connection job sync {}", request);
        jobExecutionService.scheduleConnectionSyncTask(request);
    }

    @Override
    public void runSyncConnectionNow(@Valid ScheduleConnectionSyncRequest request) {
        log.debug("Run connection sync job now {}", request);
        jobExecutionService.runConnectionSyncNow(request);
    }

    @Override
    public void runDestinationSyncNow(@Valid BaseDestinationIntegrationRequest request) {
        log.debug("Run destination sync job now {}", request);
        jobExecutionService.runDestinationSyncNow(request);
    }

}
