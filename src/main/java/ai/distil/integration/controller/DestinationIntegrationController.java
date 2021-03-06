package ai.distil.integration.controller;

import ai.distil.integration.controller.dto.BaseConnectionIntegrationRequest;
import ai.distil.integration.controller.dto.BaseDestinationIntegrationRequest;
import ai.distil.integration.controller.proxy.DestinationIntegrationProxy;
import ai.distil.integration.service.DestinationIntegrationService;
import ai.distil.model.org.destination.IntegrationSettings;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/destination")
@Api(value = "Destination Integration Controller")
public class DestinationIntegrationController implements DestinationIntegrationProxy {

    private final DestinationIntegrationService destinationIntegrationService;

    @Override
    public ResponseEntity<IntegrationSettings> getDestinationIntegrationSettings(BaseConnectionIntegrationRequest request) {
        return ResponseEntity.ok(destinationIntegrationService.retrieveDestinationIntegrationSettings(request));
    }

    @Override
    public ResponseEntity<Boolean> deleteDestinationIntegration(BaseDestinationIntegrationRequest request) {
        return ResponseEntity.ok(destinationIntegrationService.deleteJob(request));
    }

    @Override
    public ResponseEntity<Boolean> scheduleIntegrationSync(String tenantCode, Long orgId, Set<Long> segments) {
        destinationIntegrationService.runAllJobsForIntegrations(orgId, tenantCode, segments);
        return ResponseEntity.ok(true);
    }

}
