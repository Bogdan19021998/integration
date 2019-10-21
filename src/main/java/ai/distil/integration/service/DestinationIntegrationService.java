package ai.distil.integration.service;

import ai.distil.api.internal.model.dto.DTOConnection;
import ai.distil.api.internal.model.dto.DestinationIntegrationSettingsDTO;
import ai.distil.api.internal.model.dto.datasource.DTODataSourceAttributeExtended;
import ai.distil.api.internal.model.dto.destination.DestinationDTO;
import ai.distil.api.internal.model.dto.destination.DestinationIntegrationDTO;
import ai.distil.api.internal.model.dto.destination.HyperPersonalizedDestinationDTO;
import ai.distil.api.internal.proxy.ConnectionProxy;
import ai.distil.api.internal.proxy.DestinationSourceProxy;
import ai.distil.integration.controller.dto.BaseDestinationIntegrationRequest;
import ai.distil.integration.job.destination.AbstractDataSync;
import ai.distil.integration.job.sync.http.sync.SyncSettings;
import ai.distil.integration.job.sync.request.SyncDestinationRequest;
import ai.distil.integration.service.sync.ConnectionFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DestinationIntegrationService {
    private static final Integer DEFAULT_PRODUCTS_SIZE = 5;

    private final ConnectionFactory connectionFactory;
    private final ConnectionProxy connectionProxy;
    private final DestinationSourceProxy destinationSourceProxy;


    public AbstractDataSync buildDataSync(SyncDestinationRequest request, DestinationIntegrationDTO integration) {
        DTOConnection connection = connectionProxy.findOnePrivate(request.getTenantId(), request.getOrgId(), integration.getConnectionId()).getBody();

        DestinationDTO destination = destinationSourceProxy.findOneDestinationPrivate(request.getTenantId(), integration.getDestinationId()).getBody();

        Integer recommendationsCount = destination instanceof HyperPersonalizedDestinationDTO ? Optional.ofNullable((HyperPersonalizedDestinationDTO) destination)
                .map(HyperPersonalizedDestinationDTO::getNumberRecommendations)
                .orElse(DEFAULT_PRODUCTS_SIZE) : DEFAULT_PRODUCTS_SIZE;

        List<DTODataSourceAttributeExtended> attributes = destinationSourceProxy.retrieveDestinationAttributesPrivate(request.getTenantId(), request.getIntegrationId()).getBody();

        return connectionFactory.buildDataSync(destination, connection, integration, new SyncSettings(recommendationsCount), attributes);

    }

    public DestinationIntegrationSettingsDTO retrieveDestinationIntegrationSettings(BaseDestinationIntegrationRequest request) {
        DestinationIntegrationDTO integration = destinationSourceProxy.findOneByIdPrivate(request.getTenantId(), request.getIntegrationId()).getBody();
        log.info("Running sync process for the destination integration - {}", integration.getId());

        DTOConnection connection = connectionProxy.findOnePrivate(request.getTenantId(), request.getOrgId(), integration.getConnectionId()).getBody();

        AbstractDataSync dataSync = connectionFactory.buildDataSync(null, connection, integration, null, Collections.emptyList());

        return dataSync.findIntegrationSettings();
    }
}
