package ai.distil.integration.controller;

import ai.distil.api.internal.model.dto.DestinationIntegrationSettingsDTO;
import ai.distil.integration.controller.dto.BaseDestinationIntegrationRequest;
import ai.distil.integration.controller.proxy.DestinationIntegrationProxy;
import ai.distil.integration.service.DestinationIntegrationService;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/destination")
@Api(value = "Destination Integration Controller")
public class DestinationIntegrationController implements DestinationIntegrationProxy {

    private final DestinationIntegrationService destinationIntegrationService;

    @Override
    public ResponseEntity<DestinationIntegrationSettingsDTO> getDestinationIntegrationSettings(BaseDestinationIntegrationRequest request) {
        return ResponseEntity.ok(destinationIntegrationService.retrieveDestinationIntegrationSettings(request));
    }

    @Override
    public ResponseEntity<Boolean> deleteDestinationIntegration(BaseDestinationIntegrationRequest request) {
        return ResponseEntity.ok(destinationIntegrationService.deleteJob(request));
    }

}
