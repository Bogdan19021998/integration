package ai.distil.integration.controller.proxy;

import ai.distil.api.internal.model.dto.DTOConnection;
import ai.distil.api.internal.model.dto.DTODataSource;
import ai.distil.integration.controller.dto.CheckConnectivityResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(value = "ConnectionClient", url = "${ai.distil.api.integrations.url}", path = "/connection")
@Api(value = "Connection Integration Controller")
public interface ConnectionIntegrationProxy {

    @ApiOperation(value = "Check connectivity", response = CheckConnectivityResponse.class)
    @PostMapping("/check")
    ResponseEntity<CheckConnectivityResponse> checkConnectivity(@RequestBody DTOConnection dtoConnection);

    @ApiOperation(value = "Get datasource definition", response = DTODataSource.class,
            responseContainer = "List")
    @PostMapping("/datasource/get")
    ResponseEntity<List<DTODataSource>> getDatasourceDefinition(@RequestBody DTOConnection dtoConnection);

    @ApiOperation(value = "Find eligible datasource", response = DTODataSource.class,
            responseContainer = "List")
    @PostMapping("/datasource/eligible/get")
    ResponseEntity<List<DTODataSource>> findEligibleDataSources(@RequestBody DTOConnection dtoConnection);

}
