package ai.distil.integration.controller.proxy;

import ai.distil.integration.controller.dto.BaseConnectionIntegrationRequest;
import ai.distil.integration.controller.dto.BaseDestinationIntegrationRequest;
import ai.distil.model.org.destination.IntegrationSettings;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Api(value = "Connection Integration Controller")
@FeignClient(value = "DestinationIntegrationClient", url = "${ai.distil.api.integrations.url}", path = "/destination")
public interface DestinationIntegrationProxy {

    @ApiOperation(value = "Retrieve destination integration settings", response = IntegrationSettings.class)
    @PostMapping("/settings")
    ResponseEntity<IntegrationSettings> getDestinationIntegrationSettings(@RequestBody BaseConnectionIntegrationRequest request);

    @ApiOperation(value = "Delete destination integration job")
    @DeleteMapping("/job")
    ResponseEntity<Boolean> deleteDestinationIntegration(@RequestBody BaseDestinationIntegrationRequest request);

}
