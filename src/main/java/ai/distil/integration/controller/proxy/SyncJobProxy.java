package ai.distil.integration.controller.proxy;

import ai.distil.integration.controller.dto.ScheduleDatasourceSyncRequest;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;

@Api(value = "Sync job client")
@FeignClient(value = "SyncJobClient", url = "${ai.distil.api.integrations.url}", path = "/sync")
public interface SyncJobProxy {

    @ApiOperation(value = "Schedule a new sync task")
    @PostMapping("/schedule")
    void scheduleJob(@RequestBody @Valid ScheduleDatasourceSyncRequest request);

    @ApiOperation(value = "Run now a new sync task")
    @PostMapping("/run_now")
    void runSyncNow(@RequestBody @Valid ScheduleDatasourceSyncRequest request);
}
