package ai.distil.integration.job;

import ai.distil.api.internal.model.dto.DTOConnection;
import ai.distil.api.internal.model.dto.datasource.DTODataSourceAttributeExtended;
import ai.distil.api.internal.model.dto.destination.DestinationDTO;
import ai.distil.api.internal.model.dto.destination.DestinationIntegrationDTO;
import ai.distil.api.internal.model.dto.destination.HyperPersonalizedDestinationDTO;
import ai.distil.api.internal.proxy.ConnectionProxy;
import ai.distil.api.internal.proxy.DestinationSourceProxy;
import ai.distil.integration.job.destination.IDataSync;
import ai.distil.integration.job.destination.vo.CustomAttributeDefinition;
import ai.distil.integration.job.sync.http.sync.SyncSettings;
import ai.distil.integration.job.sync.request.SyncDestinationRequest;
import ai.distil.integration.service.sync.ConnectionFactory;
import ai.distil.integration.service.sync.RequestMapper;
import ai.distil.model.org.CustomerRecord;
import lombok.extern.slf4j.Slf4j;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

import static ai.distil.integration.constants.JobConstants.JOB_REQUEST;

@Slf4j
@Component
@DisallowConcurrentExecution
public class SyncDestinationIntegrationJob extends QuartzJobBean {

    private static final Integer DEFAULT_PRODUCTS_SIZE = 5;

    @Autowired
    private RequestMapper requestMapper;

    @Autowired
    private ConnectionProxy connectionProxy;

    @Autowired
    private DestinationSourceProxy destinationSourceProxy;

    @Autowired
    private ConnectionFactory connectionFactory;

    @Override
    protected void executeInternal(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        SyncDestinationRequest request = (SyncDestinationRequest) requestMapper.deserialize(jobExecutionContext.getMergedJobDataMap().getString(JOB_REQUEST),
                JobDefinitionEnum.SYNC_DESTINATION.getJobRequestClazz());

        DestinationIntegrationDTO integration = destinationSourceProxy.findOneByIdPrivate(request.getTenantId(), request.getIntegrationId()).getBody();
        log.info("Running sync process for the destination integration - {}", integration.getId());

        DTOConnection connection = connectionProxy.findOnePrivate(request.getTenantId(), request.getOrgId(), integration.getConnectionId()).getBody();

        log.info("Retrieved connection data for the integration - {}, connection type - {}", integration.getId(), connection.getConnectionType());

        DestinationDTO destination = destinationSourceProxy.findOneDestinationPrivate(request.getTenantId(), integration.getDestinationId()).getBody();

        Integer recommendationsCount = destination instanceof HyperPersonalizedDestinationDTO ? Optional.ofNullable((HyperPersonalizedDestinationDTO) destination)
                .map(HyperPersonalizedDestinationDTO::getNumberRecommendations)
                .orElse(DEFAULT_PRODUCTS_SIZE) :
                DEFAULT_PRODUCTS_SIZE;

        log.info("Defined recommendations count per sync: {}, for the integration - {}", recommendationsCount, integration.getId());

        List<DTODataSourceAttributeExtended> attributes = destinationSourceProxy.retrieveDestinationAttributesPrivate(request.getTenantId(), request.getIntegrationId()).getBody();

        log.info("Ready to sync {} attributes for the integration - {}", attributes.size(), integration.getId());

        IDataSync dataSyncService = connectionFactory.buildDataSync(connection, integration, new SyncSettings(recommendationsCount), attributes);

        String listId = dataSyncService.createListIfNotExists();
        List<CustomAttributeDefinition> createdAttributes = dataSyncService.syncCustomAttributesSchema(listId);

        List<CustomerRecord> records = destinationSourceProxy.retrieveDestinationDataPrivate(request.getTenantId(), request.getIntegrationId()).getBody();
        dataSyncService.ingestData(listId, createdAttributes, records);

    }
}
