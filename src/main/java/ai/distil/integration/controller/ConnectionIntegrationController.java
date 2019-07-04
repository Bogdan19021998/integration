package ai.distil.integration.controller;

import ai.distil.api.internal.model.dto.DTOConnection;
import ai.distil.api.internal.model.dto.DTODataSource;
import ai.distil.integration.controller.dto.CheckConnectivityResponse;
import ai.distil.integration.controller.proxy.ConnectionIntegrationProxy;
import ai.distil.integration.service.ConnectionService;
import ai.distil.integration.service.DataSyncService;
import io.swagger.annotations.Api;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/connection")
@Api(value = "Connection Integration Controller")
public class ConnectionIntegrationController implements ConnectionIntegrationProxy {

    private final ConnectionService connectionService;
    private final DataSyncService dataSyncService;

    @Override
    public ResponseEntity<CheckConnectivityResponse> checkConnectivity(DTOConnection dtoConnection) {
        return ResponseEntity.ok(new CheckConnectivityResponse(connectionService.checkConnectivity(dtoConnection)));
    }

    @Override
    public ResponseEntity<List<DTODataSource>> getDatasourceDefinition(DTOConnection dtoConnection) {
        return ResponseEntity.ok(connectionService.defineDatasource(dtoConnection));
    }

    @Override
    public ResponseEntity<List<DTODataSource>> findEligibleDataSources(DTOConnection dtoConnection) {
        return ResponseEntity.ok(dataSyncService.findAllEligibleDataSources(dtoConnection));
    }

}
