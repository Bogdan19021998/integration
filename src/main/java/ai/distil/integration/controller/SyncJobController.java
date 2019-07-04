package ai.distil.integration.controller;

import ai.distil.integration.controller.dto.ScheduleDatasourceSyncRequest;
import ai.distil.integration.service.JobExecutionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping("/sync")
@RequiredArgsConstructor
@Api(value = "Sync Controller")
public class SyncJobController {

    private final JobExecutionService jobExecutionService;

    @ApiOperation(value = "Schedule a new sync task")
    @PostMapping("/schedule")
    public void scheduleJob(@RequestBody @Valid ScheduleDatasourceSyncRequest request) {
        jobExecutionService.scheduleSyncTask(request);
    }

    @ApiOperation(value = "Run now a new sync task")
    @PostMapping("/run_now")
    public void runSyncNow(@RequestBody @Valid ScheduleDatasourceSyncRequest request) {
        jobExecutionService.runNow(request);
    }

}
