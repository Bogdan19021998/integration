package ai.distil.integration.controller;

import ai.distil.api.internal.model.dto.DTOConnection;
import ai.distil.api.internal.model.dto.DTODataSource;
import ai.distil.integration.controller.dto.CheckConnectivityResponse;
import ai.distil.integration.controller.dto.CommonConnectionRequest;
import ai.distil.integration.controller.proxy.ConnectionIntegrationProxy;
import ai.distil.integration.service.ConnectionService;
import ai.distil.integration.service.DataSyncService;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/connection")
@Api(value = "Connection Integration Controller")
public class ConnectionIntegrationController implements ConnectionIntegrationProxy {

    private final ConnectionService connectionService;
    private final DataSyncService dataSyncService;

    @Override
    public ResponseEntity<CheckConnectivityResponse> checkConnectivity(DTOConnection dtoConnection) {
        log.debug("Check connectivity for connection {}", dtoConnection.getId());
        return ResponseEntity.ok(new CheckConnectivityResponse(connectionService.checkConnectivity(dtoConnection)));
    }

    @Override
    public ResponseEntity<List<DTODataSource>> getDatasourceDefinition(DTOConnection dtoConnection) {
        log.debug("Get all data sources definitions for connection {}", dtoConnection.getId());
        return ResponseEntity.ok(connectionService.defineDatasource(dtoConnection));
    }

    @Override
    public ResponseEntity<List<DTODataSource>> findEligibleDataSources(DTOConnection dtoConnection) {
        log.debug("Get all eligible data sources for connection {}", dtoConnection.getId());
        return ResponseEntity.ok(dataSyncService.findAllEligibleDataSources(dtoConnection));
    }

    @Override
    public ResponseEntity<?> cleanConnectionData(CommonConnectionRequest request) {
        connectionService.deleteConnectionData(request);
        return ResponseEntity.ok().build();
    }

}
