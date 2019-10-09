package ai.distil.integration.service;

import ai.distil.api.internal.model.dto.DTOConnection;
import ai.distil.api.internal.model.dto.destination.DestinationIntegrationDTO;
import ai.distil.integration.job.destination.IDataSync;
import ai.distil.integration.job.destination.vo.CustomAttributeDefinition;
import ai.distil.integration.service.sync.ConnectionFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SyncDestinationService {

    private final ConnectionFactory connectionFactory;

    public void syncDestination(DTOConnection connection, DestinationIntegrationDTO integration) {
        IDataSync dataSync = connectionFactory.buildDataSync(connection, integration);

        String listId = dataSync.createListIfNotExists();
        log.info("Created list for the integration - {}, list id - {}", integration.getId(), listId);

        List<CustomAttributeDefinition> allAttributes = dataSync.syncCustomAttributesSchema(listId);

        dataSync.ingestData(listId, allAttributes);

    }
}
