package ai.distil.integration.controller.proxy;

import ai.distil.integration.controller.dto.ScheduleConnectionSyncRequest;
import ai.distil.integration.controller.dto.ScheduleDatasourceSyncRequest;
import ai.distil.integration.controller.dto.ScheduleDestinationSyncRequest;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.validation.Valid;

@Api(value = "Sync job client")
@FeignClient(value = "SyncJobClient", url = "${ai.distil.api.integrations.url}", path = "/sync")
public interface SyncJobProxy {

    @ApiOperation(value = "Schedule a new sync datasource task")
    @PostMapping("/schedule/datasource")
    void scheduleSyncDatasourceJob(@RequestBody @Valid ScheduleDatasourceSyncRequest request);

    @ApiOperation(value = "Run now a new sync datasource task")
    @PostMapping("/run_now/datasource")
    void runSyncDatasourceNow(@RequestBody @Valid ScheduleDatasourceSyncRequest request);

    @ApiOperation(value = "Schedule a new sync connection task")
    @PostMapping("/schedule/connection")
    void scheduleConnectionJob(@RequestBody @Valid ScheduleConnectionSyncRequest request);

    @ApiOperation(value = "Run now a new sync connection task")
    @PostMapping("/run_now/connection")
    void runSyncConnectionNow(@RequestBody @Valid ScheduleConnectionSyncRequest request);

    @ApiOperation(value = "Run now a new destination sync task")
    @PostMapping("/destination/run")
    void runDestinationSyncNow(@RequestBody @Valid ScheduleDestinationSyncRequest request);

}
