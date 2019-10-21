package ai.distil.integration.controller.proxy;

import ai.distil.api.internal.model.dto.DestinationIntegrationSettingsDTO;
import ai.distil.integration.controller.dto.BaseDestinationIntegrationRequest;
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

    @ApiOperation(value = "Retrieve destination integration settings", response = DestinationIntegrationSettingsDTO.class)
    @PostMapping("/settings")
    ResponseEntity<DestinationIntegrationSettingsDTO> getDestinationIntegrationSettings(@RequestBody BaseDestinationIntegrationRequest request);

    @ApiOperation(value = "Delete destination integration job")
    @DeleteMapping("/job")
    ResponseEntity<Boolean> deleteDestinationIntegration(@RequestBody BaseDestinationIntegrationRequest request);

}
